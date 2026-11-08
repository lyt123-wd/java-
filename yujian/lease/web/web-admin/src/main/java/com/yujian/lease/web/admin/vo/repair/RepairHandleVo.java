package com.yujian.lease.web.admin.vo.repair;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "报修工单处理参数")
public class RepairHandleVo {

    @Schema(description = "目标状态：1处理中 2已完成")
    private Integer status;

    @Schema(description = "处理备注")
    private String handleRemark;
}
