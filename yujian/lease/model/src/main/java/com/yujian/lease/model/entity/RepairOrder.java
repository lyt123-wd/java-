package com.yujian.lease.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Schema(description = "报修工单")
@Data
@TableName("repair_order")
public class RepairOrder extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @Schema(description = "提交用户id")
    private Long userId;

    @Schema(description = "房间id")
    private Long roomId;

    @Schema(description = "公寓id")
    private Long apartmentId;

    @Schema(description = "报修类型：水电/门窗/家电/其他")
    private String type;

    @Schema(description = "报修标题")
    private String title;

    @Schema(description = "问题描述")
    private String description;

    @Schema(description = "图片地址，多张以逗号分隔")
    private String images;

    @Schema(description = "状态：0待处理 1处理中 2已完成 3已取消")
    private Integer status;

    @Schema(description = "处理人id")
    private Long handlerId;

    @Schema(description = "处理备注")
    private String handleRemark;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;
}
