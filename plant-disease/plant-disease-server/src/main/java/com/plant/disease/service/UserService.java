package com.plant.disease.service;

import com.plant.disease.entity.User;

public interface UserService {
    String register(String username, String password);
    User login(String username, String password);
    User getById(Long id);

    int updatePassword(Long userId, String oldPassword, String newPassword);
}
