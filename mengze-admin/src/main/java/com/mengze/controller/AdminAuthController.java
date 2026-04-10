package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.ResetPasswordDto;
import com.mengze.domain.dto.SendCodeDto;
import com.mengze.domain.dto.UserLoginDto;
import com.mengze.domain.dto.VerifyResetCodeDto;
import com.mengze.domain.vo.AdminAuthVo;
import com.mengze.service.AdminAuthService;
import com.mengze.service.AuthService;
import com.mengze.service.ResetPasswordService;
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

    @Autowired
    private AuthService authService;

    @Autowired
    private ResetPasswordService resetPasswordService;

    //通过用户名密码登录
    @PostMapping("/login")
    @SystemLog(businessName = "管理员登录")
    @ApiOperation(value = "管理员登录", notes = "管理员进行登录", response = AdminAuthVo.class)
    public ResponseResult login(@RequestBody UserLoginDto dto)
    {
        return adminAuthService.login(dto);
    }

    //用户登出
    @PostMapping("/logout")
    @SystemLog(businessName = "登出")
    @ApiOperation(value = "用户登出", notes = "用户登出接口")
    public ResponseResult logout()
    {
        return adminAuthService.logout();
    }

    //获取图形验证码
    @GetMapping("/captcha")
    @SystemLog(businessName = "获取图形验证码")
    @ApiOperation(value = "图形验证码", notes = "获取图形验证码")
    public ResponseResult captcha()
    {
        return adminAuthService.captcha();
    }

    //刷新Token
    @PostMapping("/refreshToken")
    @SystemLog(businessName = "刷新Token")
    @ApiOperation(value = "刷新Token", notes = "使用刷新令牌换取新访问令牌", response = AdminAuthVo.class)
    public ResponseResult refreshToken(@RequestParam String refreshToken)
    {
        return adminAuthService.refreshToken(refreshToken);
    }

    //发送重置密码验证码
    @PostMapping("/sendCode")
    @SystemLog(businessName = "发送重置密码验证码")
    @ApiOperation(value = "重置密码验证码", notes = "获取重置密码验证码")
    public ResponseResult sendCode(@RequestBody SendCodeDto dto)
    {
        return authService.sendCode(dto);
    }

    //验证重置验证码
    @PostMapping("/verifyCode")
    @SystemLog(businessName = "验证重置密码验证码")
    @ApiOperation(value = "验证重置密码验证码", notes = "验证重置密码时的验证码是否正确")
    public ResponseResult verifyResetCode(@RequestBody VerifyResetCodeDto dto)
    {
        return resetPasswordService.verifyResetCode(dto);
    }

    //重置密码
    @PostMapping("/resetPassword")
    @SystemLog(businessName = "重置密码")
    @ApiOperation(value = "重置密码", notes = "通过验证码重置用户密码")
    public ResponseResult resetPassword(@RequestBody ResetPasswordDto dto)
    {
        return resetPasswordService.resetPassword(dto);
    }
}
