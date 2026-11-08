package com.plant.disease.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DiseaseVO {
    private Integer id;
    private String cropName;
    private String diseaseName;
    private String symptom;
    private String cause;
    private String pesticide;
    private String farming;
    private Float confidence;
    // 新增：识别时间字段
    private LocalDateTime createTime;
}
