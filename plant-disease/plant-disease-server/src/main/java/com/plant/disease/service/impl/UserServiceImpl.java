package com.plant.disease.service.impl;

import com.plant.disease.entity.User;
import com.plant.disease.mapper.UserMapper;
import com.plant.disease.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String register(String username, String password) {
        User exist = userMapper.selectByUsername(username);
        if (exist != null) {
            return "用户名已存在";
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userMapper.insert(user);
        return null;
    }

    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public int updatePassword(Long userId, String oldPassword, String newPassword) {
        // 先查询当前用户的密码哈希
        User user = userMapper.selectById(userId);
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            return 0;
        }
        String newEncoded = passwordEncoder.encode(newPassword);
        return userMapper.updatePassword(userId, user.getPassword(), newEncoded);
    }
}
