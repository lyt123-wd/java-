package com.yujian.lease.web.admin.service;

import com.yujian.lease.web.admin.vo.login.CaptchaVo;
import com.yujian.lease.web.admin.vo.login.LoginVo;
import com.yujian.lease.web.admin.vo.system.user.SystemUserInfoVo;

public interface LoginService {

    CaptchaVo getCaptcha();


    String login(LoginVo loginVo);

    SystemUserInfoVo getUserInfoById(Long userId);
}
