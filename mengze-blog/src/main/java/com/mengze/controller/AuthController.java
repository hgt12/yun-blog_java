package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.SendCodeDto;
import com.mengze.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//验证接口
@RestController
public class AuthController
{
    @Autowired
    AuthService authService;

    //发送邮箱登录验证码
    @PostMapping("/sendEmailLoginCode")
    public ResponseResult sendEmailLoginCode(@RequestBody SendCodeDto dto)
    {
        return authService.sendEmailLoginCode(dto);
    }

    //发送手机登录验证码
    @PostMapping("/sendPhoneLoginCode")
    public ResponseResult sendPhoneLoginCode(@RequestBody SendCodeDto dto)
    {
        return authService.sendPhoneLoginCode(dto);
    }

    //发送邮箱注册验证码
    @PostMapping("/sendEmailRegisterCode")
    public ResponseResult sendEmailRegisterCode(@RequestBody SendCodeDto dto)
    {
        return authService.sendEmailRegisterCode(dto);
    }

    //发送手机注册验证码
    @PostMapping("/sendPhoneRegisterCode")
    public ResponseResult sendPhoneRegisterCode(@RequestBody SendCodeDto dto)
    {
        return authService.sendPhoneRegisterCode(dto);
    }

    //发送邮箱忘记密码的验证码
    @PostMapping("/sendEmailForgotPasswordCode")
    public ResponseResult sendEmailForgotPasswordCode(@RequestBody SendCodeDto dto)
    {
        return authService.sendEmailForgotPasswordCode(dto);
    }
}
