package com.yujian.lease.web.admin.service.impl;

import com.yujian.lease.common.exception.LeaseException;
import com.yujian.lease.common.minio.MinioProperties;
import com.yujian.lease.common.result.ResultCodeEnum;
import com.yujian.lease.web.admin.service.FileService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    private final MinioClient minioClient;
    private final MinioProperties properties;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new LeaseException(ResultCodeEnum.PARAM_ERROR);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new LeaseException(ResultCodeEnum.FILE_SIZE_ERROR);
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null) {
            throw new LeaseException(ResultCodeEnum.FILE_TYPE_ERROR);
        }
        extension = extension.toLowerCase(Locale.ROOT);
        String contentType = file.getContentType();
        if (!ALLOWED_EXTENSIONS.contains(extension)
                || contentType == null
                || !contentType.startsWith("image/")) {
            throw new LeaseException(ResultCodeEnum.FILE_TYPE_ERROR);
        }

        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getBucketName()).build());
            if (!bucketExists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(properties.getBucketName()).build());
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(properties.getBucketName())
                                .config(createBucketPolicyConfig(properties.getBucketName()))
                                .build());
            }

            // 只保留扩展名，文件名由服务端生成，避免路径穿越和原始文件名注入
            String objectKey = new SimpleDateFormat("yyyyMMdd").format(new Date())
                    + "/" + UUID.randomUUID() + "." + extension;
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .contentType(contentType)
                            .bucket(properties.getBucketName())
                            .object(objectKey)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build());

            String publicEndpoint = StringUtils.hasText(properties.getPublicEndpoint())
                    ? properties.getPublicEndpoint()
                    : properties.getEndpoint();
            if (publicEndpoint.endsWith("/")) {
                publicEndpoint = publicEndpoint.substring(0, publicEndpoint.length() - 1);
            }
            return String.join("/", publicEndpoint, properties.getBucketName(), objectKey);
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new IllegalStateException("文件上传失败", e);
        }
    }

    private String createBucketPolicyConfig(String bucketName) {
        return """
            {
              "Statement" : [ {
                "Action" : "s3:GetObject",
                "Effect" : "Allow",
                "Principal" : "*",
                "Resource" : "arn:aws:s3:::%s/*"
              } ],
              "Version" : "2012-10-17"
            }
            """.formatted(bucketName);
    }
}
