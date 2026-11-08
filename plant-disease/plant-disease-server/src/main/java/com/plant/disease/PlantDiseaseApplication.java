package com.plant.disease;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.plant.disease.mapper")
public class PlantDiseaseApplication {
    public static void main(String[] args) {
        SpringApplication.run(PlantDiseaseApplication.class, args);
        System.out.println("植物病害识别系统后端启动成功");
    }
}
