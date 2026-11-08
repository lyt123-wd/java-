package com.yujian.lease.common.sms;

/**
 * 短信发送抽象：生产环境使用阿里云，开发/演示环境输出到日志。
 */
public interface SmsSender {

    void sendCode(String phone, String code);
}
