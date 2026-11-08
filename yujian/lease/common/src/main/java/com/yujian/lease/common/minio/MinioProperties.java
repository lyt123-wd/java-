package com.yujian.lease.common.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String endpoint;

    /**
     * 返回给浏览器的访问地址。容器部署时 endpoint 是内网地址，publicEndpoint 是宿主机/域名地址。
     */
    private String publicEndpoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;
}
