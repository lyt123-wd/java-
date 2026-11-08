package com.tandian.controller;

import com.tandian.dto.Result;
import com.tandian.service.IShopTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "商铺分类")
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private IShopTypeService typeService;

    @Operation(summary = "查询商铺分类列表")
    @GetMapping("/list")
    public Result queryTypeList() {
        return Result.ok(typeService.queryTypeList());
    }
}
