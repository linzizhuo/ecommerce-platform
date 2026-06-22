package com.cloudmall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CloudMall MV架构启动类
 *
 * MV两层架构: Model(Entity/Mapper) + View(Thymeleaf模板)
 * Controller直接调用Mapper, 无Service层
 */
@SpringBootApplication
@MapperScan("com.cloudmall.mapper")
public class CloudMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(CloudMallApplication.class, args);
    }
}
