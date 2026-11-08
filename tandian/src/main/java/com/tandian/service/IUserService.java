package com.tandian.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tandian.dto.LoginFormDTO;
import com.tandian.dto.Result;
import com.tandian.entity.User;

public interface IUserService extends IService<User> {

    Result sendCode(String phone);

    Result login(LoginFormDTO loginForm);

    Result logout(String token);

    Result sign();

    Result signCount();
}
