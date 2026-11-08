package com.yujian.lease.web.app.service.impl;

import com.yujian.lease.common.sms.SmsSender;
import com.yujian.lease.web.app.service.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {

    private final SmsSender smsSender;

    @Override
    public void sendSms(String phone, String code) {
        smsSender.sendCode(phone, code);
    }
}
