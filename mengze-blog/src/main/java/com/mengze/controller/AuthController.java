package com.mengze.controller;

import com.mengze.annotation.SystemLog;
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

     //发送验证码接口，邮箱/手机号两种方式，登录/注册/忘记密码时使用
    @SystemLog(businessName = "发送验证码")
    @PostMapping("/sendCode")
    public ResponseResult sendCode(@RequestBody SendCodeDto dto)
    {
        return authService.sendCode(dto);
    }
}
