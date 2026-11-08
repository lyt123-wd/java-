package com.tandian.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 从请求头中解析登录 token，兼容
 * {@code Authorization: <token>} 与 {@code Authorization: Bearer <token>} 两种写法。
 */
public final class TokenUtils {

    private static final String AUTHORIZATION_HEADER = "authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private TokenUtils() {
    }

    public static String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(AUTHORIZATION_HEADER);
        if (token == null) {
            return null;
        }
        token = token.trim();
        if (token.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            token = token.substring(BEARER_PREFIX.length()).trim();
        }
        return token.isEmpty() ? null : token;
    }
}
