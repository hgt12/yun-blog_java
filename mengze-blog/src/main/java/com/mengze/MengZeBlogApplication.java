package com.mengze;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//SpringBoot启动类
@SpringBootApplication
@MapperScan("com.mengze.mapper")//扫描mapper
public class MengZeBlogApplication
{
    public static void main(String[] args) {
        SpringApplication.run(MengZeBlogApplication.class, args);
    }
}
