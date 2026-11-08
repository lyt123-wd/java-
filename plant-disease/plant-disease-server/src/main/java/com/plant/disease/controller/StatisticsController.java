package com.plant.disease.controller;

import com.plant.disease.common.Result;
import com.plant.disease.service.RecognizeRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Resource
    private RecognizeRecordService recordService;

    @GetMapping("/diseaseType")
    public Result<List<Map<String, Object>>> diseaseCount(HttpServletRequest request) {
        // ✅ 从拦截器塞进来的request里拿userId，前端不用传了
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(recordService.getStatistics(userId));
    }
    /**
     * 近7天识别次数统计
     */
    @GetMapping("/dailyCount")
    public Result<Map<String, Object>> dailyCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        List<Map<String, Object>> list = recordService.getLast7DaysCount(userId);

        // 补全近7天日期，无数据补0
        List<String> dates = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 6; i >= 0; i--) {
            String date = today.minusDays(i).format(formatter);
            dates.add(date);
            // 查找当天数据
            int count = 0;
            for (Map<String, Object> item : list) {
                if (date.equals(item.get("date").toString())) {
                    count = ((Number) item.get("count")).intValue();
                    break;
                }
            }
            counts.add(count);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("counts", counts);
        return Result.success(result);
    }
    @GetMapping("/overview")
    public Result<Map<String, Object>> overview(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        Map<String, Object> overview = recordService.getOverview(userId);
        return Result.success(overview);
    }
    @GetMapping("/cropCount")
    public Result<List<Map<String, Object>>> cropCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(recordService.getCropCount(userId));
    }


}
