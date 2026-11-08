package com.plant.disease.mapper;

import com.plant.disease.entity.DiseaseControl;
import org.apache.ibatis.annotations.Param;

public interface DiseaseControlMapper {
    DiseaseControl selectByDiseaseId(@Param("diseaseId") Integer diseaseId);
}
