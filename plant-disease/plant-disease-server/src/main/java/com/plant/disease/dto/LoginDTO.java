// 新建一个接收登录参数的DTO，放到dto包
package com.plant.disease.dto;

import lombok.Data;
@Data
public class LoginDTO {
    private String username;
    private String password;
}
