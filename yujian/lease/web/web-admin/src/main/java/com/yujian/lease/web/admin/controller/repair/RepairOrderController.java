package com.yujian.lease.web.admin.controller.repair;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.lease.common.exception.LeaseException;
import com.yujian.lease.common.login.LoginUser;
import com.yujian.lease.common.login.LoginUserHolder;
import com.yujian.lease.common.result.Result;
import com.yujian.lease.common.result.ResultCodeEnum;
import com.yujian.lease.model.entity.RepairOrder;
import com.yujian.lease.web.admin.service.RepairOrderService;
import com.yujian.lease.web.admin.vo.repair.RepairHandleVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "报修工单-管理端")
@RestController
@RequestMapping("/admin/repair")
@RequiredArgsConstructor
public class RepairOrderController {

    private static final long MAX_PAGE_SIZE = 100L;

    private final RepairOrderService repairOrderService;

    @Operation(summary = "分页查询报修工单")
    @GetMapping("/page")
    public Result<Page<RepairOrder>> page(
            @RequestParam(value = "current", defaultValue = "1") long current,
            @RequestParam(value = "size", defaultValue = "10") long size,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "keyword", required = false) String keyword) {
        long page = Math.max(current, 1);
        long pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        return Result.ok(repairOrderService.page(page, pageSize, status, type, keyword));
    }

    @Operation(summary = "报修工单详情")
    @GetMapping("/{id}")
    public Result<RepairOrder> detail(@PathVariable("id") Long id) {
        return Result.ok(repairOrderService.getDetail(id));
    }

    @Operation(summary = "处理报修工单")
    @PutMapping("/{id}/handle")
    public Result<Void> handle(@PathVariable("id") Long id, @RequestBody RepairHandleVo vo) {
        LoginUser loginUser = LoginUserHolder.getLoginUser();
        if (loginUser == null) {
            throw new LeaseException(ResultCodeEnum.ADMIN_LOGIN_AUTH);
        }
        repairOrderService.handle(id, loginUser.getUserId(), vo);
        return Result.ok();
    }
}
