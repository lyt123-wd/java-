package com.plant.disease.controller;

import com.plant.disease.common.Result;
import com.plant.disease.entity.Disease;
import com.plant.disease.service.DiseaseService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/api/disease")
public class DiseaseController {

    @Resource
    private DiseaseService diseaseService;

    /**
     * 病害列表（支持按作物筛选）
     */
    @GetMapping("/list")
    public Result<List<Disease>> list(@RequestParam(required = false) String cropName) {
        return Result.success(diseaseService.getListByCrop(cropName));
    }

    /**
     * 病害详情（含防治方案）
     */
    @GetMapping("/{id}")
    public Result<Disease> detail(@PathVariable Integer id) {
        Disease disease = diseaseService.getDetailById(id);
        if (disease == null) {
            return Result.fail("病害信息不存在");
        }
        return Result.success(disease);
    }
}
