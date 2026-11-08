package com.plant.disease.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 项目自定义配置，全部可通过环境变量覆盖。
 */
@Data
@Component
@ConfigurationProperties(prefix = "project")
public class ProjectProperties {

    /**
     * 上传图片目录，支持相对路径和绝对路径。
     */
    private String uploadPath = "./upload/";

    /**
     * ONNX 模型路径。
     */
    private String modelPath = "./plant_disease_mobilenetv2.onnx";

    private String jwtSecret;

    private long jwtExpiration = 86_400_000L;

    public Path uploadDirectory() {
        return Paths.get(uploadPath).toAbsolutePath().normalize();
    }
}
