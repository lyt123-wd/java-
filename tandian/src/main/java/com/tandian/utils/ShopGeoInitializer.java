package com.tandian.utils;

import com.tandian.config.properties.InitProperties;
import com.tandian.entity.Shop;
import com.tandian.service.IShopService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 启动时按商铺类型建立 Redis GEO 索引，供“附近商铺”查询使用。
 * GEOADD 本身幂等，重复执行不会产生脏数据。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandian.init", name = "geo-enabled", havingValue = "true", matchIfMissing = true)
public class ShopGeoInitializer {

    private final IShopService shopService;
    private final StringRedisTemplate stringRedisTemplate;
    private final InitProperties initProperties;

    @PostConstruct
    public void loadShopGeo() {
        if (!initProperties.isGeoEnabled()) {
            return;
        }
        List<Shop> shops = shopService.list().stream()
                .filter(shop -> shop.getX() != null && shop.getY() != null)
                .toList();
        Map<Long, List<Shop>> byType = shops.stream()
                .collect(Collectors.groupingBy(Shop::getTypeId));

        byType.forEach((typeId, typeShops) -> {
            String key = RedisConstants.SHOP_GEO_KEY + typeId;
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(typeShops.size());
            for (Shop shop : typeShops) {
                locations.add(new RedisGeoCommands.GeoLocation<>(
                        shop.getId().toString(),
                        new Point(shop.getX(), shop.getY())
                ));
            }
            stringRedisTemplate.opsForGeo().add(key, locations);
        });
        log.info("商铺 GEO 索引初始化完成，共 {} 家商铺、{} 个分类", shops.size(), byType.size());
    }
}
