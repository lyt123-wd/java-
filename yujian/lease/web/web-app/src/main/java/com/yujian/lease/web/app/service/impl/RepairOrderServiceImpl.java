package com.yujian.lease.web.app.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.lease.common.exception.LeaseException;
import com.yujian.lease.common.result.ResultCodeEnum;
import com.yujian.lease.model.entity.RepairOrder;
import com.yujian.lease.model.entity.RoomInfo;
import com.yujian.lease.model.enums.RepairStatus;
import com.yujian.lease.model.mapper.RepairOrderMapper;
import com.yujian.lease.web.app.mapper.RoomInfoMapper;
import com.yujian.lease.web.app.service.RepairOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class RepairOrderServiceImpl implements RepairOrderService {

    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    private final RepairOrderMapper repairOrderMapper;
    private final RoomInfoMapper roomInfoMapper;

    @Override
    public Page<RepairOrder> pageByUserId(Long userId, long current, long size) {
        return repairOrderMapper.selectPage(
                new Page<>(current, size),
                Wrappers.<RepairOrder>lambdaQuery()
                        .eq(RepairOrder::getUserId, userId)
                        .orderByDesc(RepairOrder::getCreateTime)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RepairOrder create(Long userId, RepairOrder order) {
        if (order == null || !StringUtils.hasText(order.getTitle()) || !StringUtils.hasText(order.getType())) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        if (order.getTitle().length() > MAX_TITLE_LENGTH) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        if (order.getDescription() != null && order.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        if (order.getRoomId() != null) {
            RoomInfo room = roomInfoMapper.selectById(order.getRoomId());
            if (room == null) {
                throw new LeaseException(ResultCodeEnum.REPAIR_ROOM_NOT_FOUND);
            }
            if (order.getApartmentId() == null) {
                order.setApartmentId(room.getApartmentId());
            }
        }

        order.setId(null);
        order.setUserId(userId);
        order.setStatus(RepairStatus.PENDING);
        order.setHandlerId(null);
        order.setHandleRemark(null);
        order.setHandleTime(null);
        repairOrderMapper.insert(order);
        return repairOrderMapper.selectById(order.getId());
    }

    @Override
    public RepairOrder getDetail(Long id, Long userId) {
        RepairOrder order = repairOrderMapper.selectById(id);
        if (order == null) {
            throw new LeaseException(ResultCodeEnum.REPAIR_NOT_FOUND);
        }
        if (!userId.equals(order.getUserId())) {
            throw new LeaseException(ResultCodeEnum.REPAIR_NO_PERMISSION);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, Long userId) {
        RepairOrder order = getDetail(id, userId);
        int status = order.getStatus() == null ? RepairStatus.PENDING : order.getStatus();
        if (!RepairStatus.canCancel(status)) {
            throw new LeaseException(ResultCodeEnum.REPAIR_STATUS_ERROR);
        }
        order.setStatus(RepairStatus.CANCELED);
        repairOrderMapper.updateById(order);
    }
}
