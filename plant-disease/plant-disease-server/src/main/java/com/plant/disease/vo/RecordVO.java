package com.plant.disease.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RecordVO {
    private Long id;
    private String imageUrl;
    private String cropName;
    private String diseaseName;
    private BigDecimal confidence;
    private LocalDateTime createTime;
}
