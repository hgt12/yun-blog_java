package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UserRegisterDto;
import com.mengze.domain.entity.User;
import com.mengze.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//个人信息接口
@RestController
@RequestMapping("/user")
public class UserController
{
    @Autowired
    private UserService userService;

    //获取个人信息
    @GetMapping("/userInfo")
    public ResponseResult userInfo()
    {
        return userService.userInfo();
    }

    //更新个人信息
    @PutMapping("/userInfo")
    public ResponseResult updateUserInfo(@RequestBody User user)
    {
        return userService.updateUserInfo(user);
    }

    //用户注册
    @PostMapping("/userRegister")
    public ResponseResult userRegister(@RequestBody UserRegisterDto dto)
    {
        return userService.userRegister(dto);
    }

}
