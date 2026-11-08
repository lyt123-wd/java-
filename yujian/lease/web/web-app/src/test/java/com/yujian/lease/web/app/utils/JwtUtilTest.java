package com.yujian.lease.web.app.utils;

import com.yujian.lease.common.config.JwtProperties;
import com.yujian.lease.common.exception.LeaseException;
import com.yujian.lease.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-secret-at-least-32-bytes-long!!");
        properties.setExpiration(Duration.ofMinutes(5));
        jwtUtil = new JwtUtil(properties);
    }

    @Test
    void shouldRoundTripClaims() {
        String token = jwtUtil.createToken(7L, "alice");
        Claims claims = jwtUtil.parseToken(token);

        assertEquals(7L, claims.get("userId", Long.class));
        assertEquals("alice", claims.get("username", String.class));
    }

    @Test
    void shouldRejectInvalidToken() {
        assertThrows(LeaseException.class, () -> jwtUtil.parseToken("not-a-token"));
    }

    @Test
    void shouldRejectShortSecret() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("too-short");
        assertThrows(IllegalStateException.class, () -> new JwtUtil(properties));
    }
}
