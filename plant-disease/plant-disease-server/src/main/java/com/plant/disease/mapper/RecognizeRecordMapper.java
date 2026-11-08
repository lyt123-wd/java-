package com.plant.disease.mapper;

import com.plant.disease.entity.RecognizeRecord;
import com.plant.disease.vo.RecordDetailVO;
import com.plant.disease.vo.RecordVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

public interface RecognizeRecordMapper {
    int insert(RecognizeRecord record);

    List<RecordVO> selectByUserId(@Param("userId") Long userId);

    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    List<Map<String, Object>> countByDisease(@Param("userId") Long userId);

    List<RecordVO> selectPageRecord(@Param("userId") Long userId, @Param("offset") int offset, @Param("pageSize") int pageSize);

    Long countRecord(@Param("userId") Long userId);

    RecordDetailVO selectDetailById(@Param("id") Long id, @Param("userId") Long userId);

    List<Map<String, Object>> countLast7Days(@Param("userId") Long userId);

    long countTotal(@Param("userId") Long userId);

    long countDiseaseTypeNum(@Param("userId") Long userId);

    long countToday(@Param("userId") Long userId);

    long countLast7DaysTotal(@Param("userId") Long userId);

    List<Map<String, Object>> countByCrop(@Param("userId") Long userId);
}
