package com.tandian.config;

import com.tandian.config.properties.AdminProperties;
import com.tandian.config.properties.StorageProperties;
import com.tandian.utils.AdminInterceptor;
import com.tandian.utils.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class MvcConfig implements WebMvcConfigurer {

    private final StringRedisTemplate stringRedisTemplate;
    private final StorageProperties storageProperties;
    private final AdminProperties adminProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 管理端写接口：GET 放行，写操作校验 X-Admin-Token
        registry.addInterceptor(new AdminInterceptor(adminProperties))
                .addPathPatterns(
                        "/shop", "/shop/**",
                        "/voucher", "/voucher/**",
                        "/voucher-order/admin", "/voucher-order/admin/**")
                .order(0);

        // 用户登录态
        registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
                .excludePathPatterns(
                        // 登录相关
                        "/user/code",
                        "/user/login",
                        // 公开浏览（/shop、/voucher 的写接口由 AdminInterceptor 保护）
                        "/shop",
                        "/shop/**",
                        "/shop-type/**",
                        "/voucher",
                        "/voucher/**",
                        // 秒杀运维接口由管理端 token 单独保护
                        "/voucher-order/admin",
                        "/voucher-order/admin/**",
                        "/blog/hot",
                        // 上传后的静态资源
                        "/imgs/**",
                        // 接口文档与健康检查
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/actuator/health/**",
                        "/error"
                )
                .order(1);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = storageProperties.uploadPath();
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new IllegalStateException("无法创建上传目录: " + uploadPath, e);
        }
        String location = uploadPath.toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/imgs/**").addResourceLocations(location);
    }
}
