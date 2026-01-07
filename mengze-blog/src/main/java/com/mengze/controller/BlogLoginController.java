package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.User;
import com.mengze.service.BlogLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//登录接口
@RestController
public class BlogLoginController
{
    @Autowired
    private BlogLoginService blogLoginService;

    @PostMapping("/login")
    public ResponseResult login(@RequestBody User user)
    {
        return blogLoginService.login(user);
    }

}
