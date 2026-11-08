package com.yujian.lease.web.app.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.lease.model.entity.RepairOrder;

public interface RepairOrderService {

    Page<RepairOrder> pageByUserId(Long userId, long current, long size);

    RepairOrder create(Long userId, RepairOrder order);

    RepairOrder getDetail(Long id, Long userId);

    void cancel(Long id, Long userId);
}
