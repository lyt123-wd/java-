package com.plant.disease.controller;

import com.plant.disease.common.Result;
import com.plant.disease.dto.LoginDTO;
import com.plant.disease.entity.User;
import com.plant.disease.service.UserService;
import com.plant.disease.utils.JwtUtil;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()
                || dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            return Result.fail("用户名和密码不能为空");
        }
        User user = userService.login(dto.getUsername(), dto.getPassword());
        if (user == null) {
            return Result.fail("用户名或密码错误");
        }
        String token = JwtUtil.createToken(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userId", user.getId());
        data.put("username", user.getUsername());
        return Result.success(data);
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody LoginDTO dto) {
        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()
                || dto.getPassword() == null || dto.getPassword().trim().isEmpty()) {
            return Result.fail("用户名和密码不能为空");
        }
        if (dto.getPassword().length() < 6) {
            return Result.fail("密码长度不能少于6位");
        }
        String msg = userService.register(dto.getUsername(), dto.getPassword());
        if (msg != null) {
            return Result.fail(msg);
        }
        return Result.success("注册成功");
    }

    @PostMapping("/updatePassword")
    public Result<Integer> updatePassword(@RequestBody Map<String, String> params, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("currentUserId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        if (oldPassword == null || newPassword == null) {
            return Result.fail("原密码和新密码不能为空");
        }
        if (newPassword.length() < 6) {
            return Result.fail("新密码长度不能少于6位");
        }
        int result = userService.updatePassword(userId, oldPassword, newPassword);
        if (result > 0) {
            return Result.success(result);
        } else {
            return Result.fail("原密码错误，修改失败");
        }
    }
}
