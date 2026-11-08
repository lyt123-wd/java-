package com.plant.disease.service.impl;

import com.plant.disease.entity.RecognizeRecord;
import com.plant.disease.mapper.RecognizeRecordMapper;
import com.plant.disease.service.RecognizeRecordService;
import com.plant.disease.vo.RecordDetailVO;
import com.plant.disease.vo.RecordVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecognizeRecordServiceImpl implements RecognizeRecordService {

    @Resource
    private RecognizeRecordMapper recordMapper;

    @Override
    public int addRecord(RecognizeRecord record) {
        // 统一设置识别时间为当前系统时间
        record.setCreateTime(LocalDateTime.now());
        return recordMapper.insert(record);
    }

    @Override
    public List<RecordVO> getListByUserId(Long userId) {
        return recordMapper.selectByUserId(userId);
    }

    @Override
    public int deleteRecord(Long id, Long userId) {
        return recordMapper.deleteById(id, userId);
    }

    @Override
    public List<Map<String, Object>> getStatistics(Long userId) {
        return recordMapper.countByDisease(userId);
    }

    @Override
    public List<RecordVO> getPageListByUserId(Long userId, int offset, Integer pageSize) {
        return recordMapper.selectPageRecord(userId, offset, pageSize);
    }

    @Override
    public Long countByUserId(Long userId) {
        return recordMapper.countRecord(userId);
    }

    @Override
    public RecordDetailVO getDetailById(Long id, Long userId) {
        return recordMapper.selectDetailById(id, userId);
    }

    @Override
    public List<Map<String, Object>> getLast7DaysCount(Long userId) {
        return recordMapper.countLast7Days(userId);
    }

    @Override
    public Map<String, Object> getOverview(Long userId) {
        long totalCount = recordMapper.countTotal(userId);
        long diseaseTypeCount = recordMapper.countDiseaseTypeNum(userId);
        long todayCount = recordMapper.countToday(userId);
        long weekCount = recordMapper.countLast7DaysTotal(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("totalCount", totalCount);
        result.put("diseaseTypeCount", diseaseTypeCount);
        result.put("todayCount", todayCount);
        result.put("weekCount", weekCount);
        return result;
    }

    @Override
    public List<Map<String, Object>> getCropCount(Long userId) {
        return recordMapper.countByCrop(userId);
    }
}
