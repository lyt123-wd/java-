package com.tandian.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tandian.entity.ShopType;
import com.tandian.mapper.ShopTypeMapper;
import com.tandian.service.IShopTypeService;
import com.tandian.utils.RedisConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ShopType> queryTypeList() {
        String key = RedisConstants.CACHE_SHOP_TYPE_KEY;
        String cached = stringRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            return JSONUtil.toList(cached, ShopType.class);
        }

        List<ShopType> list = query().orderByAsc("sort").list();
        if (!list.isEmpty()) {
            stringRedisTemplate.opsForValue().set(
                    key, JSONUtil.toJsonStr(list),
                    RedisConstants.CACHE_SHOP_TYPE_TTL_MINUTES, TimeUnit.MINUTES);
        }
        return list;
    }
}
