package com.plant.disease.service;

import com.plant.disease.entity.Disease;
import com.plant.disease.vo.DiseaseVO;
import java.util.List;

public interface DiseaseService {
    List<Disease> getListByCrop(String cropName);
    Disease getDetailById(Integer id);
    DiseaseVO getDiseaseDetail(Integer diseaseId);
    DiseaseVO getDiseaseDetailByEnName(String enName);
}

