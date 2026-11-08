package com.yujian.lease.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * JWT 配置。secret 通过环境变量 JWT_SECRET 注入，长度至少 32 字节。
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;

    private Duration expiration = Duration.ofDays(7);
}
