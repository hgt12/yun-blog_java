package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LoginDto;
import com.mengze.domain.dto.ResetPasswordDto;
import com.mengze.domain.dto.VerifyResetCodeDto;
import com.mengze.domain.entity.User;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import com.mengze.service.BlogLoginService;
import com.mengze.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//登录接口
@RestController
public class BlogLoginController
{
    @Autowired
    private BlogLoginService blogLoginService;

    @Autowired
    private UserService userService;

    //通过用户名密码登录
    @PostMapping("/login")
    public ResponseResult login(@RequestBody User user)
    {
        //预防编程，用户名是否为空
        if (!StringUtils.hasText(user.getUsername()))
        {
            // 提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);//需要用户名
        }
        return blogLoginService.login(user);
    }

    @PostMapping("logout")
    public ResponseResult logout()
    {
        return blogLoginService.logout();
    }
    
    //通过邮箱登录
    @PostMapping("/loginByEmail")
    public ResponseResult loginByEmail(@RequestBody LoginDto dto)
    {
        return blogLoginService.loginByEmail(dto);
    }
    
    //通过手机登录
    @PostMapping("/loginByPhone")
    public ResponseResult loginByPhone(@RequestBody LoginDto dto)
    {
        return blogLoginService.loginByPhone(dto);
    }

    //验证重置验证码
    @PostMapping("/verifyResetCode")
    public ResponseResult verifyResetCode(@RequestBody VerifyResetCodeDto dto)
    {
        return blogLoginService.verifyResetCode(dto);
    }

    //重置密码
    @PostMapping("/resetPassword")
    public ResponseResult resetPassword(@RequestBody ResetPasswordDto dto)
    {
        return blogLoginService.resetPassword(dto);
    }

}
