package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LoginDto;
import com.mengze.domain.dto.ResetPasswordDto;
import com.mengze.domain.dto.UserLoginDto;
import com.mengze.domain.dto.VerifyResetCodeDto;
import com.mengze.domain.vo.BlogUserLoginVo;
import com.mengze.service.BlogLoginService;
import com.mengze.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//登录接口
@RestController
@Api(tags = "登录", description = "用户登录相关接口")
public class BlogLoginController
{
    @Autowired
    private BlogLoginService blogLoginService;

    @Autowired
    private UserService userService;

    //通过用户名密码登录
    @PostMapping("/login")
    @SystemLog(businessName = "用户名密码登录")
    @ApiOperation(value = "用户名密码登录", notes = "通过用户名和密码进行登录", response = BlogUserLoginVo.class)
    public ResponseResult login(@RequestBody UserLoginDto dto)
    {
        return blogLoginService.login(dto);
    }

    @PostMapping("logout")
    @SystemLog(businessName = "用户登出")
    @ApiOperation(value = "用户登出", notes = "退出登录")
    public ResponseResult logout()
    {
        return blogLoginService.logout();
    }
    
    //通过邮箱登录
    @PostMapping("/loginByEmail")
    @SystemLog(businessName = "邮箱验证码登录")
    @ApiOperation(value = "邮箱验证码登录", notes = "通过邮箱和验证码进行登录", response = BlogUserLoginVo.class)
    public ResponseResult loginByEmail(@RequestBody LoginDto dto)
    {
        return blogLoginService.loginByEmail(dto);
    }
    
    //通过手机登录
    @PostMapping("/loginByPhone")
    @SystemLog(businessName = "手机验证码登录")
    @ApiOperation(value = "手机验证码登录", notes = "通过手机号和验证码进行登录", response = BlogUserLoginVo.class)
    public ResponseResult loginByPhone(@RequestBody LoginDto dto)
    {
        return blogLoginService.loginByPhone(dto);
    }

    //验证重置验证码
    @PostMapping("/verifyResetCode")
    @SystemLog(businessName = "验证重置密码验证码")
    @ApiOperation(value = "验证重置密码验证码", notes = "验证重置密码时的验证码是否正确")
    public ResponseResult verifyResetCode(@RequestBody VerifyResetCodeDto dto)
    {
        return blogLoginService.verifyResetCode(dto);
    }

    //重置密码
    @PostMapping("/resetPassword")
    @SystemLog(businessName = "重置密码")
    @ApiOperation(value = "重置密码", notes = "通过验证码重置用户密码")
    public ResponseResult resetPassword(@RequestBody ResetPasswordDto dto)
    {
        return blogLoginService.resetPassword(dto);
    }

}
