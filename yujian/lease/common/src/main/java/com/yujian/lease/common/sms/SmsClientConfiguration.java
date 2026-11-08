package com.yujian.lease.common.sms;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.teaopenapi.models.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 根据配置选择短信发送实现：
 * - 开启且配置了 AccessKey：阿里云短信
 * - 否则：控制台输出（方便本地开发和演示）
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(SmsProperties.class)
public class SmsClientConfiguration {

    @Bean
    public SmsSender smsSender(SmsProperties properties) {
        if (properties.isEnabled()
                && StringUtils.hasText(properties.getAccessKeyId())
                && StringUtils.hasText(properties.getAccessKeySecret())) {
            log.info("短信通道：阿里云短信，templateCode={}", properties.getTemplateCode());
            return new AliyunSmsSender(properties);
        }
        log.info("短信通道：控制台输出（未启用阿里云短信），验证码会打印在日志中");
        return (phone, code) -> log.info("[DEV SMS] phone={}, code={}", phone, code);
    }

    /**
     * 阿里云短信实现。
     */
    private static class AliyunSmsSender implements SmsSender {

        private final SmsProperties properties;
        private final Client client;

        private AliyunSmsSender(SmsProperties properties) {
            this.properties = properties;
            Config config = new Config()
                    .setEndpoint(properties.getEndpoint())
                    .setAccessKeyId(properties.getAccessKeyId())
                    .setAccessKeySecret(properties.getAccessKeySecret());
            try {
                this.client = new Client(config);
            } catch (Exception e) {
                throw new IllegalStateException("初始化阿里云短信客户端失败", e);
            }
        }

        @Override
        public void sendCode(String phone, String code) {
            com.aliyun.dysmsapi20170525.models.SendSmsRequest request =
                    new com.aliyun.dysmsapi20170525.models.SendSmsRequest()
                            .setPhoneNumbers(phone)
                            .setTemplateCode(properties.getTemplateCode())
                            .setTemplateParam("{\"code\":\"" + code + "\"}")
                            .setSignName(properties.getSignName());
            try {
                client.sendSms(request);
            } catch (Exception e) {
                throw new IllegalStateException("发送短信失败", e);
            }
        }
    }
}
