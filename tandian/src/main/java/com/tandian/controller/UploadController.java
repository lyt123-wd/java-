package com.tandian.controller;

import cn.hutool.core.util.StrUtil;
import com.tandian.config.properties.StorageProperties;
import com.tandian.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Tag(name = "文件上传")
@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
public class UploadController {

    private final StorageProperties storageProperties;

    @Operation(summary = "上传探店笔记图片")
    @PostMapping("/blog")
    public Result uploadImage(@RequestParam("file") MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return Result.fail("文件不能为空");
        }
        String originalFilename = image.getOriginalFilename();
        String suffix = StrUtil.subAfter(originalFilename, ".", true);
        if (StrUtil.isBlank(suffix) || !storageProperties.allowedExtensionSet()
                .contains(suffix.toLowerCase())) {
            return Result.fail("不支持的图片格式");
        }

        String name = UUID.randomUUID().toString().replace("-", "");
        int hash = name.hashCode();
        int d1 = hash & 0xF;
        int d2 = (hash >> 4) & 0xF;

        Path dir = storageProperties.uploadPath().resolve(Path.of("blogs", String.valueOf(d1), String.valueOf(d2)));
        try {
            Files.createDirectories(dir);
            Path target = dir.resolve(name + "." + suffix.toLowerCase());
            image.transferTo(target);

            String relative = "/blogs/" + d1 + "/" + d2 + "/" + target.getFileName();
            log.info("文件上传成功: {}", relative);
            return Result.ok("/imgs" + relative);
        } catch (IOException e) {
            throw new IllegalStateException("文件上传失败", e);
        }
    }

    @Operation(summary = "删除探店笔记图片")
    @GetMapping("/blog/delete")
    public Result deleteBlogImg(@RequestParam("name") String filename) {
        if (StrUtil.isBlank(filename)) {
            return Result.fail("文件名不能为空");
        }
        String relative = filename.trim().replace('\\', '/');
        if (relative.startsWith("/imgs/")) {
            relative = relative.substring("/imgs".length());
        }
        while (relative.startsWith("/")) {
            relative = relative.substring(1);
        }

        Path root = storageProperties.uploadPath();
        Path target = root.resolve(relative).normalize();
        // 防止 ../ 路径穿越删除上传目录之外的文件
        if (!target.startsWith(root) || Files.isDirectory(target)) {
            return Result.fail("非法的文件路径");
        }
        try {
            Files.deleteIfExists(target);
            return Result.ok();
        } catch (IOException e) {
            throw new IllegalStateException("删除文件失败", e);
        }
    }
}
