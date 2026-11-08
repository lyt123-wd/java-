package com.yujian.lease.common.sms;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "aliyun.sms")
public class SmsProperties {

    /**
     * 是否启用阿里云短信；关闭时验证码打印到应用日志。
     */
    private boolean enabled = false;

    private String accessKeyId;

    private String accessKeySecret;

    private String endpoint = "dysmsapi.aliyuncs.com";

    private String templateCode;

    private String signName;
}
