package com.plant.disease.mapper;

import com.plant.disease.entity.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    User selectByUsername(@Param("username") String username);

    int insert(User user);

    User selectById(@Param("id") Long id);

    int updatePassword(@Param("userId") Long userId, @Param("oldPassword") String oldPassword, @Param("newPassword") String newPassword);

}