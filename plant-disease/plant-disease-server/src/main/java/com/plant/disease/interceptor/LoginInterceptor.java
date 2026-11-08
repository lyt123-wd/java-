package com.plant.disease.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.plant.disease.utils.JwtUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class LoginInterceptor implements HandlerInterceptor {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = resolveToken(request);
        if (token == null) {
            writeUnauthorizedResponse(response, "未携带token，请登录");
            return false;
        }
        try {
            Long userId = JwtUtil.getUserId(token);
            request.setAttribute("currentUserId", userId);
            return true;
        } catch (Exception e) {
            writeUnauthorizedResponse(response, "token无效或者已过期，请重新登录");
            return false;
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader("token");
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

    private void writeUnauthorizedResponse(HttpServletResponse response, String message) throws Exception {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(401);
        Map<String, Object> res = new HashMap<>();
        res.put("code", 401);
        res.put("msg", message);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(MAPPER.writeValueAsString(res));
            writer.flush();
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        request.removeAttribute("currentUserId");
    }
}
