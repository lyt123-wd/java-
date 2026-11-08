package com.plant.disease.controller;

import com.plant.disease.common.Result;
import com.plant.disease.service.RecognizeRecordService;
import com.plant.disease.vo.RecordDetailVO;
import com.plant.disease.vo.RecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "识别记录")
@RestController
@RequestMapping("/api/record")
public class RecordController {

    @Resource
    private RecognizeRecordService recordService;

    @Operation(summary = "识别记录列表")
    @GetMapping("/list")
    public Result<List<RecordVO>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        return Result.success(recordService.getListByUserId(userId));
    }

    @Operation(summary = "删除识别记录")
    @DeleteMapping("/{id}")
    public Result<Integer> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        // service 内按 userId 限定删除条件，避免越权删除他人记录
        return Result.success(recordService.deleteRecord(id, userId));
    }

    @Operation(summary = "分页查询识别记录")
    @GetMapping("/page")
    public Result<Map<String, Object>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "5") Integer pageSize,
            HttpServletRequest request
    ) {
        Long userId = (Long) request.getAttribute("currentUserId");
        int offset = (pageNum - 1) * pageSize;
        List<RecordVO> list = recordService.getPageListByUserId(userId, offset, pageSize);
        Long total = (long) recordService.countByUserId(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("records", list);
        map.put("total", total);
        return Result.success(map);
    }

    @Operation(summary = "识别记录详情")
    @GetMapping("/{id}")
    public Result<RecordDetailVO> getDetail(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        RecordDetailVO detail = recordService.getDetailById(id, userId);
        return Result.success(detail);
    }

    @Operation(summary = "导出识别记录 CSV")
    @GetMapping("/export")
    public void export(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long userId = (Long) request.getAttribute("currentUserId");
        List<RecordVO> records = recordService.getListByUserId(userId);

        response.setContentType("text/csv;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=recognize-records.csv");

        try (PrintWriter writer = response.getWriter()) {
            // BOM，保证 Excel 打开中文不乱码
            writer.write('﻿');
            writer.println("记录ID,作物,病害,置信度,识别时间");
            for (RecordVO record : records) {
                writer.println(String.join(",",
                        String.valueOf(record.getId()),
                        csv(record.getCropName()),
                        csv(record.getDiseaseName()),
                        record.getConfidence() == null ? "" : record.getConfidence().stripTrailingZeros().toPlainString(),
                        record.getCreateTime() == null ? "" : record.getCreateTime().toString()
                ));
            }
        }
    }

    private String csv(String value) {
        if (value == null) {
            return "";
        }
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
