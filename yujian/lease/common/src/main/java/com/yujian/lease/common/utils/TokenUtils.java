package com.yujian.lease.common.utils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 兼容前端使用的 access-token 请求头与标准的 Authorization / Bearer 写法。
 */
public final class TokenUtils {

    private static final String BEARER_PREFIX = "Bearer ";

    private TokenUtils() {
    }

    public static String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("access-token");
        if (token == null) {
            token = request.getHeader("authorization");
        }
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
