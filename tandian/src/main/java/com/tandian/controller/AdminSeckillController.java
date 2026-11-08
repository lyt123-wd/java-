package com.tandian.controller;

import com.tandian.config.properties.SeckillProperties;
import com.tandian.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.Limit;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 秒杀 Stream 运维接口，需要 X-Admin-Token。
 */
@Tag(name = "秒杀运维")
@RestController
@RequestMapping("/voucher-order/admin")
@RequiredArgsConstructor
public class AdminSeckillController {

    private static final long MAX_PAGE_SIZE = 100L;

    private final StringRedisTemplate stringRedisTemplate;
    private final SeckillProperties seckillProperties;

    @Operation(summary = "查询 pending 消息统计")
    @GetMapping("/stream/pending")
    public Result pending() {
        PendingMessagesSummary summary = stringRedisTemplate.opsForStream()
                .pending(seckillProperties.getStreamKey(), seckillProperties.getConsumerGroup());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("group", summary.getGroupName());
        data.put("totalPendingMessages", summary.getTotalPendingMessages());
        data.put("minMessageId", summary.minMessageId());
        data.put("maxMessageId", summary.maxMessageId());
        data.put("perConsumer", summary.getPendingMessagesPerConsumer());
        return Result.ok(data);
    }

    @Operation(summary = "查询死信队列")
    @GetMapping("/stream/dead")
    public Result dead(@RequestParam(value = "count", defaultValue = "20") long count) {
        long limit = Math.min(Math.max(count, 1), MAX_PAGE_SIZE);
        List<Map<String, Object>> records = readRecords(Range.unbounded(), limit);
        return Result.ok(records);
    }

    @Operation(summary = "重放一条死信消息")
    @PostMapping("/stream/dead/replay/{id}")
    public Result replay(@PathVariable("id") String id) {
        List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream().range(
                deadLetterKey(),
                Range.closed(id, id),
                Limit.limit().count(1)
        );
        if (records == null || records.isEmpty()) {
            return Result.fail("死信消息不存在");
        }

        MapRecord<String, Object, Object> record = records.get(0);
        stringRedisTemplate.opsForStream().add(
                seckillProperties.getStreamKey(),
                new HashMap<>(record.getValue())
        );
        stringRedisTemplate.opsForStream().delete(deadLetterKey(), id);
        return Result.ok();
    }

    private List<Map<String, Object>> readRecords(Range<String> range, long count) {
        return stringRedisTemplate.opsForStream()
                .range(deadLetterKey(), range, Limit.limit().count((int) count))
                .stream()
                .map(record -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", record.getId().getValue());
                    item.put("fields", record.getValue());
                    return item;
                })
                .toList();
    }

    private String deadLetterKey() {
        return seckillProperties.getStreamKey() + ".dead";
    }
}
