package com.tandian.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 管理端写接口的简单鉴权配置。
 * 生产环境应替换为完整的后台用户 + 角色权限模型。
 */
@Data
@Component
@ConfigurationProperties(prefix = "tandian.admin")
public class AdminProperties {

    private String token = "change-me";
}
