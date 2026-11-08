package com.tandian.utils;

import com.tandian.entity.SeckillVoucher;
import com.tandian.service.ISeckillVoucherService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时把数据库库存预热到 Redis。
 * 使用 setIfAbsent，避免应用重启把已经扣减过的 Redis 库存重置回数据库旧值。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandian.init", name = "stock-enabled", havingValue = "true", matchIfMissing = true)
public class SeckillStockPreLoader {

    private final ISeckillVoucherService seckillVoucherService;
    private final StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void loadAllSeckillStock() {
        List<SeckillVoucher> list = seckillVoucherService.list();
        int loaded = 0;
        for (SeckillVoucher voucher : list) {
            String key = RedisConstants.SECKILL_STOCK_KEY + voucher.getVoucherId();
            Boolean absent = stringRedisTemplate.opsForValue()
                    .setIfAbsent(key, voucher.getStock().toString());
            if (Boolean.TRUE.equals(absent)) {
                loaded++;
            }
        }
        log.info("秒杀库存预热完成，新增 {} 个 key，共 {} 张秒杀券", loaded, list.size());
    }
}
