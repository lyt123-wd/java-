package com.yujian.lease.web.app.service;

import com.yujian.lease.web.app.vo.user.LoginVo;
import com.yujian.lease.web.app.vo.user.UserInfoVo;

public interface LoginService {
    void sendCode(String phone);

    String login(LoginVo loginVo);

    UserInfoVo getLoginUserInfoById(Long userId);
}
