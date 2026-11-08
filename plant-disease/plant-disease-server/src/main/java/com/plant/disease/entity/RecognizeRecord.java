package com.plant.disease.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class RecognizeRecord {
    private Long id;
    private Long userId;
    private String imageUrl;
    private Integer diseaseId;
    private BigDecimal confidence;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


}
