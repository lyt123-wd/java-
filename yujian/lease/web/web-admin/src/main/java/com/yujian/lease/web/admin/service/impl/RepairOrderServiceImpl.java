package com.yujian.lease.web.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.lease.common.exception.LeaseException;
import com.yujian.lease.common.result.ResultCodeEnum;
import com.yujian.lease.model.entity.RepairOrder;
import com.yujian.lease.model.enums.RepairStatus;
import com.yujian.lease.model.mapper.RepairOrderMapper;
import com.yujian.lease.web.admin.service.RepairOrderService;
import com.yujian.lease.web.admin.vo.repair.RepairHandleVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl implements RepairOrderService {

    private final RepairOrderMapper repairOrderMapper;

    @Override
    public Page<RepairOrder> page(long current, long size, Integer status, String type, String keyword) {
        LambdaQueryWrapper<RepairOrder> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(status != null, RepairOrder::getStatus, status)
                .eq(StringUtils.hasText(type), RepairOrder::getType, type);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(RepairOrder::getTitle, keyword)
                    .or()
                    .like(RepairOrder::getDescription, keyword));
        }
        wrapper.orderByDesc(RepairOrder::getCreateTime);
        return repairOrderMapper.selectPage(new Page<>(current, size), wrapper);
    }

    @Override
    public RepairOrder getDetail(Long id) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new LeaseException(ResultCodeEnum.REPAIR_NOT_FOUND);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handle(Long id, Long handlerId, RepairHandleVo vo) {
        if (vo == null || vo.getStatus() == null
                || (vo.getStatus() != RepairStatus.PROCESSING && vo.getStatus() != RepairStatus.COMPLETED)) {
            throw new LeaseException(ResultCodeEnum.REPAIR_HANDLE_STATUS_ERROR);
        }

        RepairOrder order = getDetail(id);
        int currentStatus = order.getStatus() == null ? RepairStatus.PENDING : order.getStatus();
        if (!RepairStatus.canHandle(currentStatus)) {
            throw new LeaseException(ResultCodeEnum.REPAIR_STATUS_ERROR);
        }

        order.setStatus(vo.getStatus());
        order.setHandlerId(handlerId);
        order.setHandleRemark(vo.getHandleRemark());
        order.setHandleTime(LocalDateTime.now());
        repairOrderMapper.updateById(order);
    }
}
