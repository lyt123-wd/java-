package com.tandian.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 秒杀订单消息队列配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "tandian.seckill")
public class SeckillProperties {

    private String streamKey = "stream.orders";

    private String consumerGroup = "g1";

    private String consumerName = "c1";

    /**
     * Stream 近似最大长度，防止消息无限堆积。
     */
    private long streamMaxLen = 10_000L;

    /**
     * 同一用户每秒允许的秒杀请求数。
     */
    private int rateLimitPerSecond = 5;
}
