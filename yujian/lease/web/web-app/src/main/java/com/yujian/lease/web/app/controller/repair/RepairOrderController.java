package com.yujian.lease.web.app.controller.repair;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.lease.common.exception.LeaseException;
import com.yujian.lease.common.login.LoginUser;
import com.yujian.lease.common.login.LoginUserHolder;
import com.yujian.lease.common.result.Result;
import com.yujian.lease.common.result.ResultCodeEnum;
import com.yujian.lease.model.entity.RepairOrder;
import com.yujian.lease.web.app.service.RepairOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "报修工单-用户端")
@RestController
@RequestMapping("/app/repair")
@RequiredArgsConstructor
public class RepairOrderController {

    private static final long MAX_PAGE_SIZE = 50L;

    private final RepairOrderService repairOrderService;

    @Operation(summary = "提交报修")
    @PostMapping
    public Result<RepairOrder> create(@RequestBody RepairOrder order) {
        return Result.ok(repairOrderService.create(currentUserId(), order));
    }

    @Operation(summary = "我的报修列表")
    @GetMapping("/my")
    public Result<Page<RepairOrder>> myList(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size) {
        long page = Math.max(current, 1);
        long pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return Result.ok(repairOrderService.pageByUserId(currentUserId(), page, pageSize));
    }

    @Operation(summary = "报修详情")
    @GetMapping("/{id}")
    public Result<RepairOrder> detail(@PathVariable("id") Long id) {
        return Result.ok(repairOrderService.getDetail(id, currentUserId()));
    }

    @Operation(summary = "取消报修")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable("id") Long id) {
        repairOrderService.cancel(id, currentUserId());
        return Result.ok();
    }

    private Long currentUserId() {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_AUTH);
        }
        return loginUser.getUserId();
    }
}
