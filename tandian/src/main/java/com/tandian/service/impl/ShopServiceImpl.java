package com.tandian.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tandian.dto.Result;
import com.tandian.entity.Shop;
import com.tandian.mapper.ShopMapper;
import com.tandian.service.IShopService;
import com.tandian.utils.RedisConstants;
import com.tandian.utils.SystemConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 商铺服务：缓存旁路 + Redisson 互斥锁防止缓存击穿。
 */
@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public Result queryById(Long id) {
        return Result.ok(queryWithMutex(id));
    }

    private Shop queryWithMutex(Long id) {
        String key = RedisConstants.CACHE_SHOP_KEY + id;

        // 1. 先查缓存
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(cached)) {
            return JSONUtil.toBean(cached, Shop.class);
        }
        // 空字符串代表缓存了不存在的商铺，直接返回，防止缓存穿透
        if (cached != null) {
            return null;
        }

        // 2. 缓存未命中，用分布式锁保证只有一个请求回源数据库
        RLock lock = redissonClient.getLock(RedisConstants.LOCK_SHOP_KEY + id);
        boolean locked = false;
        try {
            locked = lock.tryLock(RedisConstants.LOCK_SHOP_WAIT_SECONDS,
                    RedisConstants.LOCK_SHOP_LEASE_SECONDS, TimeUnit.SECONDS);
            if (!locked) {
                // 降级：拿不到锁时直接查库，避免请求长时间阻塞
                log.warn("获取商铺缓存锁超时，降级查询数据库, shopId={}", id);
                return getById(id);
            }

            // 3. 双重检查，防止在等锁期间别的线程已经重建缓存
            cached = stringRedisTemplate.opsForValue().get(key);
            if (StrUtil.isNotBlank(cached)) {
                return JSONUtil.toBean(cached, Shop.class);
            }
            if (cached != null) {
                return null;
            }

            Shop shop = getById(id);
            if (shop == null) {
                stringRedisTemplate.opsForValue()
                        .set(key, "", RedisConstants.CACHE_NULL_TTL_MINUTES, TimeUnit.MINUTES);
                return null;
            }
            stringRedisTemplate.opsForValue()
                    .set(key, JSONUtil.toJsonStr(shop), RedisConstants.CACHE_SHOP_TTL_MINUTES, TimeUnit.MINUTES);
            return shop;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("获取商铺缓存锁被中断", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Override
    public Result update(Shop shop) {
        if (shop == null || shop.getId() == null) {
            return Result.fail("商铺 id 不能为空");
        }
        updateById(shop);
        stringRedisTemplate.delete(RedisConstants.CACHE_SHOP_KEY + shop.getId());
        return Result.ok();
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        int page = current == null || current < 1 ? 1 : current;

        // 不带坐标时走数据库分页
        if (x == null || y == null) {
            Page<Shop> result = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(page, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(result.getRecords());
        }

        int from = (page - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = page * SystemConstants.DEFAULT_PAGE_SIZE;
        String key = RedisConstants.SHOP_GEO_KEY + typeId;

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo().search(
                key,
                GeoReference.fromCoordinate(x, y),
                new Distance(5000),
                RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
        );
        if (results == null || results.getContent().isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        if (list.size() <= from) {
            return Result.ok(Collections.emptyList());
        }

        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(item -> {
            String shopId = item.getContent().getName();
            ids.add(Long.valueOf(shopId));
            distanceMap.put(shopId, item.getDistance());
        });
        if (ids.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }

        String idStr = StrUtil.join(",", ids);
        List<Shop> shops = query().in("id", ids).last("ORDER BY FIELD(id," + idStr + ")").list();
        for (Shop shop : shops) {
            Distance distance = distanceMap.get(shop.getId().toString());
            if (distance != null) {
                shop.setDistance(distance.getValue());
            }
        }
        return Result.ok(shops);
    }
}
