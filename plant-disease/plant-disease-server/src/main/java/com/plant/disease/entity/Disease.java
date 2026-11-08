package com.plant.disease.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Disease {
    private Integer id;
    private String cropName;
    private String diseaseName;
    private String enName;
    private LocalDateTime createTime;

    // 防治方案字段（从 disease_control 表关联查询）
    private String symptom;
    private String cause;
    private String pesticide;
    private String farming;
}

