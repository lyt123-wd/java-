package com.tandian.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tandian.config.properties.SeckillProperties;
import com.tandian.dto.Result;
import com.tandian.dto.UserDTO;
import com.tandian.entity.SeckillVoucher;
import com.tandian.entity.VoucherOrder;
import com.tandian.mapper.VoucherOrderMapper;
import com.tandian.service.ISeckillVoucherService;
import com.tandian.service.IVoucherOrderService;
import com.tandian.utils.RedisConstants;
import com.tandian.utils.RedisIdWorker;
import com.tandian.utils.SeckillMetrics;
import com.tandian.utils.SeckillRateLimiter;
import com.tandian.utils.UserHolder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀下单：
 * Lua 原子校验库存 + 一人一单并写入 Redis Stream ->
 * 单线程消费者异步落库 -> 失败消息留在 pending-list 重试 -> 超过次数进入死信 Stream。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder>
        implements IVoucherOrderService {

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    private final ISeckillVoucherService seckillVoucherService;
    private final RedisIdWorker redisIdWorker;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;
    private final SeckillProperties seckillProperties;
    private final SeckillRateLimiter seckillRateLimiter;
    private final SeckillMetrics seckillMetrics;

    /**
     * 延迟获取自身代理，避免循环依赖，同时保证 createVoucherOrder 的 @Transactional 生效。
     */
    private final ObjectProvider<IVoucherOrderService> selfProvider;

    private final ExecutorService orderExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "seckill-order-consumer");
        thread.setDaemon(true);
        return thread;
    });

    private volatile boolean running = true;

    private static final int MAX_DELIVERY_COUNT = 5;

    @PostConstruct
    public void start() {
        initConsumerGroup();
        orderExecutor.submit(this::consumeLoop);
    }

    @PreDestroy
    public void stop() {
        running = false;
        orderExecutor.shutdownNow();
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        if (voucher == null) {
            seckillMetrics.failure();
            return Result.fail("优惠券不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getBeginTime().isAfter(now)) {
            seckillMetrics.failure();
            return Result.fail("秒杀尚未开始");
        }
        if (voucher.getEndTime().isBefore(now)) {
            seckillMetrics.failure();
            return Result.fail("秒杀已经结束");
        }

        UserDTO user = UserHolder.getUser();
        if (user == null) {
            seckillMetrics.failure();
            return Result.fail("请先登录");
        }
        Long userId = user.getId();

        // 用户级限流，防止脚本刷接口
        if (!seckillRateLimiter.allow(userId, seckillProperties.getRateLimitPerSecond())) {
            seckillMetrics.rateLimited();
            return Result.fail("操作过于频繁，请稍后再试");
        }

        long orderId = redisIdWorker.nextId("order");

        // Lua 脚本内完成：库存校验、一人一单校验、扣减库存、记录用户、发送 Stream 消息
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString(),
                String.valueOf(orderId),
                seckillProperties.getStreamKey(),
                String.valueOf(seckillProperties.getStreamMaxLen())
        );
        if (result == null) {
            seckillMetrics.failure();
            return Result.fail("秒杀失败，请稍后重试");
        }
        int code = result.intValue();
        if (code == 1) {
            seckillMetrics.stockEmpty();
            return Result.fail("库存不足");
        }
        if (code == 2) {
            seckillMetrics.duplicate();
            return Result.fail("不能重复下单");
        }
        seckillMetrics.success();
        return Result.ok(orderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        Long voucherId = voucherOrder.getVoucherId();

        Long count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            // 极端情况下 Redis 与数据库状态不一致，补齐 Redis 标记，避免继续尝试
            stringRedisTemplate.opsForSet().add(
                    RedisConstants.SECKILL_ORDER_KEY + voucherId, userId.toString());
            log.warn("用户重复下单，已忽略: userId={}, voucherId={}", userId, voucherId);
            return;
        }

        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();
        if (!success) {
            rollbackRedisReservation(voucherOrder);
            log.error("扣减数据库库存失败，已回滚 Redis 预占: userId={}, voucherId={}", userId, voucherId);
            return;
        }

        if (!save(voucherOrder)) {
            rollbackRedisReservation(voucherOrder);
            throw new IllegalStateException("保存订单失败");
        }
    }

    private void rollbackRedisReservation(VoucherOrder order) {
        stringRedisTemplate.opsForValue().increment(
                RedisConstants.SECKILL_STOCK_KEY + order.getVoucherId());
        stringRedisTemplate.opsForSet().remove(
                RedisConstants.SECKILL_ORDER_KEY + order.getVoucherId(),
                order.getUserId().toString());
    }

    private void initConsumerGroup() {
        try {
            stringRedisTemplate.opsForStream().createGroup(
                    seckillProperties.getStreamKey(),
                    ReadOffset.from("0"),
                    seckillProperties.getConsumerGroup()
            );
            log.info("Redis Stream 消费组创建成功: stream={}, group={}",
                    seckillProperties.getStreamKey(), seckillProperties.getConsumerGroup());
        } catch (Exception e) {
            String message = e.getMessage();
            if (message != null && message.contains("BUSYGROUP")) {
                log.info("Redis Stream 消费组已存在: {}", seckillProperties.getConsumerGroup());
            } else {
                log.warn("Redis Stream 消费组创建失败，消费线程稍后重试", e);
            }
        }
    }

    private void consumeLoop() {
        // 先处理上次进程退出时遗留的 pending 消息
        try {
            handlePendingList();
        } catch (Exception e) {
            log.warn("启动时处理 pending-list 失败，稍后重试", e);
        }

        while (running) {
            try {
                List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream().read(
                        Consumer.from(seckillProperties.getConsumerGroup(), seckillProperties.getConsumerName()),
                        StreamReadOptions.empty().count(1).block(Duration.ofSeconds(1)),
                        StreamOffset.create(seckillProperties.getStreamKey(), ReadOffset.lastConsumed())
                );
                if (records == null || records.isEmpty()) {
                    continue;
                }
                for (MapRecord<String, Object, Object> record : records) {
                    processRecord(record);
                }
            } catch (Exception e) {
                if (!running) {
                    break;
                }
                String message = e.getMessage();
                if (message != null && message.contains("NOGROUP")) {
                    initConsumerGroup();
                }
                log.error("秒杀订单消费异常，转入 pending-list 重试", e);
                sleepQuietly(1000);
                try {
                    handlePendingList();
                } catch (Exception ex) {
                    log.error("pending-list 处理异常", ex);
                    sleepQuietly(1000);
                }
            }
        }
    }

    /**
     * 处理当前消费者 pending-list 中未 ACK 的消息；失败次数过多的消息转 dead-letter。
     */
    private void handlePendingList() {
        String streamKey = seckillProperties.getStreamKey();
        String group = seckillProperties.getConsumerGroup();

        while (running) {
            PendingMessages pending = stringRedisTemplate.opsForStream()
                    .pending(streamKey, group, Range.unbounded(), 100);
            if (pending == null || pending.isEmpty()) {
                return;
            }

            Map<String, Long> deliveryCounts = new HashMap<>(pending.size());
            for (PendingMessage pendingMessage : pending) {
                deliveryCounts.put(pendingMessage.getIdAsString(), pendingMessage.getTotalDeliveryCount());
            }

            List<MapRecord<String, Object, Object>> records = stringRedisTemplate.opsForStream().read(
                    Consumer.from(group, seckillProperties.getConsumerName()),
                    StreamReadOptions.empty().count(10),
                    StreamOffset.create(streamKey, ReadOffset.from("0"))
            );
            if (records == null || records.isEmpty()) {
                return;
            }

            for (MapRecord<String, Object, Object> record : records) {
                String recordId = record.getId().getValue();
                long deliveryCount = deliveryCounts.getOrDefault(recordId, 0L);
                if (deliveryCount > MAX_DELIVERY_COUNT) {
                    deadLetter(record);
                    acknowledge(record);
                    continue;
                }
                try {
                    processRecord(record);
                } catch (Exception e) {
                    log.error("pending 消息处理失败，稍后重试, id={}, deliveryCount={}",
                            recordId, deliveryCount, e);
                    return;
                }
            }
        }
    }

    private void processRecord(MapRecord<String, Object, Object> record) {
        VoucherOrder order = BeanUtil.fillBeanWithMap(record.getValue(), new VoucherOrder(), true);
        handleVoucherOrder(order);
        acknowledge(record);
    }

    private void handleVoucherOrder(VoucherOrder voucherOrder) {
        Long userId = voucherOrder.getUserId();
        RLock lock = redissonClient.getLock(RedisConstants.LOCK_ORDER_KEY + userId);
        boolean locked = false;
        try {
            locked = lock.tryLock(3, 30, TimeUnit.SECONDS);
            if (!locked) {
                // 抛异常让消息留在 pending-list，下一轮继续处理，不能直接 ACK 丢掉订单
                throw new IllegalStateException("获取下单锁失败: userId=" + userId);
            }
            selfProvider.getObject().createVoucherOrder(voucherOrder);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("等待下单锁被中断: userId=" + userId, e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void acknowledge(MapRecord<String, Object, Object> record) {
        stringRedisTemplate.opsForStream().acknowledge(
                seckillProperties.getStreamKey(),
                seckillProperties.getConsumerGroup(),
                record.getId().getValue()
        );
    }

    private void deadLetter(MapRecord<String, Object, Object> record) {
        String deadLetterKey = seckillProperties.getStreamKey() + ".dead";
        Map<Object, Object> content = new HashMap<>(record.getValue());
        content.put("failedAt", LocalDateTime.now().toString());
        stringRedisTemplate.opsForStream().add(deadLetterKey, content);
        log.error("消息超过最大重试次数，转入死信队列: stream={}, id={}", deadLetterKey, record.getId());
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
