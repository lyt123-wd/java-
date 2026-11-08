package com.plant.disease.entity;

import lombok.Data;

@Data
public class DiseaseControl {
    private Integer id;
    private Integer diseaseId;
    private String symptom;
    private String cause;
    private String pesticide;
    private String farming;
}
