package com.mengze.domain.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//分页配置类
@Configuration
public class PaginationConfig
{
    /**
     * 3.4.0之后版本
     * @return
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(){
        MybatisPlusInterceptor mybatisPlusInterceptor = new
                MybatisPlusInterceptor();
        mybatisPlusInterceptor.addInnerInterceptor(new
                PaginationInnerInterceptor());
        return mybatisPlusInterceptor;
    }
}
