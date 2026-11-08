package com.tandian.utils;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeckillMetricsTest {

    @Test
    void shouldCountSeckillResults() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SeckillMetrics metrics = new SeckillMetrics(registry);
        metrics.init();

        metrics.success();
        metrics.success();
        metrics.stockEmpty();
        metrics.rateLimited();

        assertEquals(2.0, registry.get("seckill.requests").tag("result", "success").counter().count());
        assertEquals(1.0, registry.get("seckill.requests").tag("result", "stock_empty").counter().count());
        assertEquals(1.0, registry.get("seckill.requests").tag("result", "rate_limited").counter().count());
    }
}
