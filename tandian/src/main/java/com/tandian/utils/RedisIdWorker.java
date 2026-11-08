package com.tandian.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 自增的全局唯一 ID：
 * 符号位(1) + 时间戳(31) + 序列号(32)。
 */
@Component
public class RedisIdWorker {

    /**
     * 2022-01-01T00:00:00Z 对应的秒级时间戳。
     */
    private static final long EPOCH_SECONDS = 1640995200L;

    private static final long COUNT_BITS = 32;
    private static final long MAX_COUNT = (1L << COUNT_BITS) - 1;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StringRedisTemplate stringRedisTemplate;

    public RedisIdWorker(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public long nextId(String keyPrefix) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        long timestamp = today.atStartOfDay().toEpochSecond(ZoneOffset.UTC) - EPOCH_SECONDS;

        String counterKey = RedisConstants.ID_WORKER_KEY + keyPrefix + ":" + today.format(DAY_FORMATTER);
        Long count = stringRedisTemplate.opsForValue().increment(counterKey);
        if (count == null) {
            throw new IllegalStateException("生成全局 ID 失败");
        }
        if (count == 1L) {
            // 计数器保留两天，避免 Redis key 无限增长
            stringRedisTemplate.expire(counterKey, 2, TimeUnit.DAYS);
        }
        if (count > MAX_COUNT) {
            throw new IllegalStateException("同一秒内生成的 ID 数量超出上限: " + count);
        }
        return timestamp << COUNT_BITS | count;
    }
}
