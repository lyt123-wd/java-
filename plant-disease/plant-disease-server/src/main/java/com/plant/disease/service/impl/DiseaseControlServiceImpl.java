package com.plant.disease.service.impl;

import com.plant.disease.entity.Disease;
import com.plant.disease.entity.DiseaseControl;
import com.plant.disease.mapper.DiseaseControlMapper;
import com.plant.disease.mapper.DiseaseMapper;
import com.plant.disease.service.DiseaseControlService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DiseaseControlServiceImpl implements DiseaseControlService {

    @Resource
    private DiseaseMapper diseaseMapper;
    @Resource
    private DiseaseControlMapper controlMapper;

    @Override
    public Disease getDiseaseById(Integer id) {
        return diseaseMapper.selectById(id);
    }

    @Override
    public DiseaseControl getControlByDiseaseId(Integer diseaseId) {
        return controlMapper.selectByDiseaseId(diseaseId);
    }
}
