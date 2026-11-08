package com.tandian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tandian.entity.ShopType;

import java.util.List;

public interface IShopTypeService extends IService<ShopType> {

    /**
     * 查询商铺分类列表，带 Redis 缓存。
     */
    List<ShopType> queryTypeList();
}
