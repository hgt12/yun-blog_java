package com.mengze;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

//启动类
@SpringBootApplication
@MapperScan("com.mengze.mapper")//扫描mapper
@EnableScheduling
@EnableAsync
@EnableSwagger2
public class MengZeBlogApplication
{
    public static void main(String[] args) {
        SpringApplication.run(MengZeBlogApplication.class, args);
    }
}
