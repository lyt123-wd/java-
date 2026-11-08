package com.tandian.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * 秒杀接口用户级限流：同一用户每秒最多 N 次请求。
 * INCR + EXPIRE + 阈值判断放在 Lua 中，保证原子性。
 */
@Component
@RequiredArgsConstructor
public class SeckillRateLimiter {

    private static final DefaultRedisScript<Long> RATE_SCRIPT;

    static {
        RATE_SCRIPT = new DefaultRedisScript<>();
        RATE_SCRIPT.setLocation(new ClassPathResource("seckill-rate.lua"));
        RATE_SCRIPT.setResultType(Long.class);
    }

    private final StringRedisTemplate stringRedisTemplate;

    public boolean allow(Long userId, int limitPerSecond) {
        Long result = stringRedisTemplate.execute(
                RATE_SCRIPT,
                Collections.singletonList(RedisConstants.SECKILL_RATE_KEY + userId),
                String.valueOf(limitPerSecond)
        );
        return result != null && result == 1L;
    }
}
