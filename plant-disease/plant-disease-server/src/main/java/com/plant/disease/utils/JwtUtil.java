package com.plant.disease.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 工具类，密钥和过期时间从配置读取。
 * 密钥通过 JWT_SECRET 环境变量注入，长度至少 32 字节。
 */
@Component
public class JwtUtil {

    private static String secretStr;
    private static long expirationTime;

    @Value("${project.jwt-secret}")
    public void setSecretStr(String secret) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("project.jwt-secret 至少需要 32 字节，请通过 JWT_SECRET 配置");
        }
        JwtUtil.secretStr = secret;
    }

    @Value("${project.jwt-expiration}")
    public void setExpirationTime(long expirationTime) {
        if (expirationTime <= 0) {
            throw new IllegalStateException("project.jwt-expiration 必须大于 0");
        }
        JwtUtil.expirationTime = expirationTime;
    }

    private static SecretKey getKey() {
        if (secretStr == null) {
            throw new IllegalStateException("JWT 密钥尚未初始化");
        }
        return Keys.hmacShaKeyFor(secretStr.getBytes(StandardCharsets.UTF_8));
    }

    public static String createToken(Long userId) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(userId.toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationTime))
                .signWith(getKey())
                .compact();
    }

    public static Long getUserId(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return Long.parseLong(claims.getSubject());
    }
}
