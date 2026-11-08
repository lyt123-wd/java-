package com.tandian.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 启动时缓存初始化开关（本地开发可关闭）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "tandian.init")
public class InitProperties {

    /**
     * 是否在启动时把商铺坐标写入 Redis GEO。
     */
    private boolean geoEnabled = true;

    /**
     * 是否在启动时把秒杀券库存预热到 Redis。
     */
    private boolean stockEnabled = true;
}
