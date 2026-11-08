package com.plant.disease.service.impl;

import com.plant.disease.entity.Disease;
import com.plant.disease.mapper.DiseaseMapper;
import com.plant.disease.service.DiseaseService;
import com.plant.disease.vo.DiseaseVO;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class DiseaseServiceImpl implements DiseaseService {

    @Resource
    private DiseaseMapper diseaseMapper;

    @Override
    public List<Disease> getListByCrop(String cropName) {
        return diseaseMapper.selectListByCrop(cropName);
    }

    @Override
    public Disease getDetailById(Integer id) {
        return diseaseMapper.selectDetailById(id);
    }

    @Override
    public DiseaseVO getDiseaseDetail(Integer diseaseId) {
        return diseaseMapper.getDiseaseDetail(diseaseId);
    }

    @Override
    public DiseaseVO getDiseaseDetailByEnName(String enName) {
        return diseaseMapper.getDiseaseDetailByEnName(enName);
    }
}

