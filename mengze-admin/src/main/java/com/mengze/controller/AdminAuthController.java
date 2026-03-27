package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UserLoginDto;
import com.mengze.domain.vo.AdminAuthVo;
import com.mengze.service.AdminAuthService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//后台验证相关接口
@RestController
@RequestMapping("/auth")
@Api(tags = "验证与登录", description = "验证与登录相关接口")
public class AdminAuthController
{
    @Autowired
    private AdminAuthService adminAuthService;

    //通过用户名密码登录
    @PostMapping("/login")
    @SystemLog(businessName = "管理员登录")
    @ApiOperation(value = "管理员登录", notes = "管理员进行登录", response = AdminAuthVo.class)
    public ResponseResult login(@RequestBody UserLoginDto dto)
    {
        return adminAuthService.login(dto);
    }

    //获取图形验证码
    @GetMapping("/captcha")
    @SystemLog(businessName = "获取图形验证码")
    @ApiOperation(value = "图形验证码", notes = "获取图形验证码", response = AdminAuthVo.class)
    public ResponseResult captcha()
    {
        return adminAuthService.captcha(null);
    }
}
