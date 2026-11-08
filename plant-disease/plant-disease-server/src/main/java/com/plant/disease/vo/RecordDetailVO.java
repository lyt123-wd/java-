package com.plant.disease.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecordDetailVO {
    private Long id;
    private String imageUrl;
    private String cropName;
    private String diseaseName;
    private BigDecimal confidence;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    // 防治建议字段
    private String symptom;
    private String cause;
    private String pesticide;
    private String farming;
}
