package com.mengze.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

//该类的作用
//启用 Spring Security。
//定义哪些接口可以匿名访问（白名单）。
//其他接口必须认证。
//禁用 Session，适合前后端分离 + Token 认证场景。
//预留了异常处理和自定义过滤器的位置。
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                //关闭csrf
                .csrf().disable()
                //不通过Session获取SecurityContext
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                // 对于登录接口 允许匿名访问
                .antMatchers("/login").anonymous()
                // 对于热门文章列表接口 允许匿名访问
                .antMatchers("/article/hotArticleList").permitAll()
                // 对于文章分类列表接口 允许匿名访问
                .antMatchers("/category/getCategoryList").permitAll()
                // 对于分页查询文章列表接口 允许匿名访问
                .antMatchers("/article/articleList").permitAll()
                // 对于文章详情接口 允许匿名访问
                .antMatchers("/article/{id}").permitAll()
                // 除上面外的所有请求全部需要鉴权认证
                .anyRequest().authenticated();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}

