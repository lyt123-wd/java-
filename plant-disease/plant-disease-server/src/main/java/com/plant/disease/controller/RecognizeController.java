package com.plant.disease.controller;

import com.plant.disease.common.Result;
import com.plant.disease.entity.RecognizeRecord;
import com.plant.disease.service.DiseaseService;
import com.plant.disease.service.InferenceService;
import com.plant.disease.service.RecognizeRecordService;
import com.plant.disease.vo.DiseaseVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "病害识别")
@RestController
@RequestMapping("/api/recognize")
@RequiredArgsConstructor
public class RecognizeController {

    private static final int MAX_BATCH_SIZE = 10;

    private final InferenceService inferenceService;
    private final DiseaseService diseaseService;
    private final RecognizeRecordService recognizeRecordService;

    @Operation(summary = "上传单张图片识别")
    @PostMapping("/upload")
    public Result<DiseaseVO> recognize(@RequestParam("file") MultipartFile file,
                                       HttpServletRequest request) {
        try {
            return Result.success(recognizeAndRecord(file, currentUserId(request)));
        } catch (IllegalArgumentException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.error("图片识别失败，请上传有效的图片文件");
        }
    }

    @Operation(summary = "批量识别（最多 10 张）")
    @PostMapping("/batch")
    public Result<List<Map<String, Object>>> recognizeBatch(@RequestParam("files") MultipartFile[] files,
                                                            HttpServletRequest request) {
        if (files == null || files.length == 0) {
            return Result.fail("请选择图片");
        }
        if (files.length > MAX_BATCH_SIZE) {
            return Result.fail("一次最多识别 " + MAX_BATCH_SIZE + " 张图片");
        }

        Long userId = currentUserId(request);
        List<Map<String, Object>> results = new ArrayList<>(files.length);
        for (MultipartFile file : files) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("fileName", file.getOriginalFilename());
            try {
                item.put("success", true);
                item.put("data", recognizeAndRecord(file, userId));
            } catch (IllegalArgumentException e) {
                item.put("success", false);
                item.put("error", e.getMessage());
            } catch (Exception e) {
                item.put("success", false);
                item.put("error", "识别失败");
            }
            results.add(item);
        }
        return Result.success(results);
    }

    private DiseaseVO recognizeAndRecord(MultipartFile file, Long userId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择图片");
        }

        byte[] bytes = file.getBytes();
        Map<String, Object> predictMap = inferenceService.predict(bytes);
        Integer classIndex = (Integer) predictMap.get("classIndex");
        String className = (String) predictMap.get("className");
        Float confidence = (Float) predictMap.get("confidence");
        String imageUrl = (String) predictMap.get("imageUrl");

        // 按 class name 匹配数据库，避免模型类别顺序与 disease_info.id 绑定
        DiseaseVO vo = diseaseService.getDiseaseDetailByEnName(normalizeClassName(className));
        if (vo == null) {
            // 兼容旧数据：class_names.txt 中缺少某个类别时回退到按序号查询
            vo = diseaseService.getDiseaseDetail(classIndex + 1);
        }
        if (vo == null) {
            throw new IllegalStateException("没有找到该病害信息");
        }
        vo.setConfidence(confidence);
        vo.setCreateTime(LocalDateTime.now());

        RecognizeRecord record = new RecognizeRecord();
        record.setUserId(userId);
        record.setImageUrl(imageUrl);
        record.setDiseaseId(vo.getId() != null ? vo.getId() : classIndex + 1);
        record.setConfidence(new BigDecimal(String.valueOf(confidence)));
        recognizeRecordService.addRecord(record);

        return vo;
    }

    private String normalizeClassName(String className) {
        if ("x_Removed_from_Healthy_leaves".equals(className)) {
            return "Background_without_leaves";
        }
        return className;
    }

    private Long currentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("currentUserId");
    }
}
