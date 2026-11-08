package com.tandian.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 本地上传文件配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "tandian.storage")
public class StorageProperties {

    /**
     * 上传根目录，支持相对路径（相对于应用启动目录）和绝对路径。
     */
    private String uploadDir = "./uploads";

    /**
     * 允许上传的图片后缀，逗号分隔。
     */
    private String allowedExtensions = "jpg,jpeg,png,webp,gif";

    public Path uploadPath() {
        return Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public Set<String> allowedExtensionSet() {
        return Arrays.stream(allowedExtensions.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toUnmodifiableSet());
    }
}
