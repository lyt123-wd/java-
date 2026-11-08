package com.plant.disease.service;

import com.plant.disease.entity.Disease;
import com.plant.disease.entity.DiseaseControl;

public interface DiseaseControlService {
    Disease getDiseaseById(Integer id);
    DiseaseControl getControlByDiseaseId(Integer diseaseId);
}
