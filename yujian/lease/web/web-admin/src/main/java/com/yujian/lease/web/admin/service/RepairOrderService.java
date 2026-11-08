package com.yujian.lease.web.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yujian.lease.model.entity.RepairOrder;
import com.yujian.lease.web.admin.vo.repair.RepairHandleVo;

public interface RepairOrderService {

    Page<RepairOrder> page(long current, long size, Integer status, String type, String keyword);

    RepairOrder getDetail(Long id);

    void handle(Long id, Long handlerId, RepairHandleVo vo);
}
