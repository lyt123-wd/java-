package com.tandian.controller;

import cn.hutool.core.bean.BeanUtil;
import com.tandian.dto.LoginFormDTO;
import com.tandian.dto.Result;
import com.tandian.dto.UserDTO;
import com.tandian.entity.User;
import com.tandian.entity.UserInfo;
import com.tandian.service.IUserInfoService;
import com.tandian.service.IUserService;
import com.tandian.utils.TokenUtils;
import com.tandian.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户")
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    @Operation(summary = "发送手机验证码")
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone) {
        return userService.sendCode(phone);
    }

    @Operation(summary = "验证码登录")
    @PostMapping("/login")
    public Result login(@Valid @RequestBody LoginFormDTO loginForm) {
        return userService.login(loginForm);
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        return userService.logout(TokenUtils.resolveToken(request));
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public Result me() {
        return Result.ok(UserHolder.getUser());
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/info/{id}")
    public Result info(@PathVariable("id") Long userId) {
        UserInfo info = userInfoService.getById(userId);
        if (info == null) {
            return Result.ok();
        }
        info.setCreateTime(null);
        info.setUpdateTime(null);
        return Result.ok(info);
    }

    @Operation(summary = "按 id 查询用户")
    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId) {
        User user = userService.getById(userId);
        if (user == null) {
            return Result.ok();
        }
        return Result.ok(BeanUtil.copyProperties(user, UserDTO.class));
    }

    @Operation(summary = "每日签到")
    @PostMapping("/sign")
    public Result sign() {
        return userService.sign();
    }

    @Operation(summary = "查询本月连续签到天数")
    @GetMapping("/sign/count")
    public Result signCount() {
        return userService.signCount();
    }
}
