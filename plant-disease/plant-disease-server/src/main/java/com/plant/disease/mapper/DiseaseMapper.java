package com.plant.disease.mapper;

import com.plant.disease.entity.Disease;
import com.plant.disease.vo.DiseaseVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface DiseaseMapper {
    Disease selectById(Integer id);
    List<Disease> selectAll();
    // 新增：联表查询病害完整详情
    DiseaseVO getDiseaseDetail(@Param("diseaseId") Integer diseaseId);
    DiseaseVO getDiseaseDetailByEnName(@Param("enName") String enName);
    List<Disease> selectListByCrop(@Param("cropName") String cropName);
    Disease selectDetailById(@Param("id") Integer id);

}


