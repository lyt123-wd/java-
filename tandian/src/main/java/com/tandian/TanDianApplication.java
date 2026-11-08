package com.tandian;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.tandian.mapper")
@SpringBootApplication
public class TanDianApplication {

    public static void main(String[] args) {
        SpringApplication.run(TanDianApplication.class, args);
    }

}
