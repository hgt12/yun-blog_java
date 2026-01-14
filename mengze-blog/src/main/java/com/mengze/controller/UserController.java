package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//个人信息接口
@RestController
@RequestMapping("/user")
public class UserController
{
    @Autowired
    private UserService userService;

    @GetMapping("userInfo")
    public ResponseResult userInfo()
    {
        return userService.userInfo();
    }
}
