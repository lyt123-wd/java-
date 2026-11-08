package com.tandian.utils;

import cn.hutool.json.JSONUtil;
import com.tandian.config.properties.AdminProperties;
import com.tandian.dto.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * 管理端写接口鉴权：GET 放行，POST/PUT/DELETE 需要携带 X-Admin-Token。
 */
public class AdminInterceptor implements HandlerInterceptor {

    private static final String ADMIN_TOKEN_HEADER = "X-Admin-Token";

    private final AdminProperties adminProperties;

    public AdminInterceptor(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        // 秒杀运维接口即使 GET 也必须带管理端 token
        boolean alwaysRequireToken = request.getRequestURI().startsWith("/voucher-order/admin");
        if (!alwaysRequireToken && ("GET".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method))) {
            return true;
        }

        String token = request.getHeader(ADMIN_TOKEN_HEADER);
        if (token != null && MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8),
                adminProperties.getToken().getBytes(StandardCharsets.UTF_8))) {
            return true;
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.fail("无权执行该操作")));
        return false;
    }
}
