package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.SendCodeDto;
import com.mengze.service.AuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//验证接口
@RestController
@Api(tags = "验证", description = "验证码相关接口")
public class AuthController
{
    @Autowired
    AuthService authService;

     //发送验证码接口，邮箱/手机号两种方式，登录/注册/忘记密码时使用
    @PostMapping("/sendCode")
    @SystemLog(businessName = "发送验证码")
    @ApiOperation(value = "发送验证码", notes = "发送邮箱或手机验证码，用于登录/注册/忘记密码")
    public ResponseResult sendCode(@RequestBody SendCodeDto dto)
    {
        return authService.sendCode(dto);
    }
}
