package com.tandian.utils;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 秒杀结果指标，Prometheus 抓取 /actuator/prometheus。
 */
@Component
@RequiredArgsConstructor
public class SeckillMetrics {

    private final MeterRegistry meterRegistry;

    private Counter success;
    private Counter stockEmpty;
    private Counter duplicate;
    private Counter rateLimited;
    private Counter failure;

    @PostConstruct
    public void init() {
        success = counter("success");
        stockEmpty = counter("stock_empty");
        duplicate = counter("duplicate");
        rateLimited = counter("rate_limited");
        failure = counter("failure");
    }

    private Counter counter(String result) {
        return Counter.builder("seckill.requests")
                .description("秒杀请求结果统计")
                .tag("result", result)
                .register(meterRegistry);
    }

    public void success() {
        success.increment();
    }

    public void stockEmpty() {
        stockEmpty.increment();
    }

    public void duplicate() {
        duplicate.increment();
    }

    public void rateLimited() {
        rateLimited.increment();
    }

    public void failure() {
        failure.increment();
    }
}
