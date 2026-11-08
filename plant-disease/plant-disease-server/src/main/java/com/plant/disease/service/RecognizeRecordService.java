package com.plant.disease.service;

import com.plant.disease.entity.RecognizeRecord;
import com.plant.disease.vo.RecordDetailVO;
import com.plant.disease.vo.RecordVO;

import java.util.List;
import java.util.Map;

public interface RecognizeRecordService {
    int addRecord(RecognizeRecord record);
    List<RecordVO> getListByUserId(Long userId);
    int deleteRecord(Long id, Long userId);
    List<Map<String, Object>> getStatistics(Long userId);

    List<RecordVO> getPageListByUserId(Long userId, int offset, Integer pageSize);

    Long countByUserId(Long userId);

    RecordDetailVO getDetailById(Long id, Long userId);

    List<Map<String, Object>> getLast7DaysCount(Long userId);

    Map<String, Object> getOverview(Long userId);

    List<Map<String, Object>> getCropCount(Long userId);
}
