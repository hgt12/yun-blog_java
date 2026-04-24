package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UpdateUserInfoDto;
import com.mengze.domain.dto.UserRegisterDto;
import com.mengze.domain.vo.BloggerInfoVo;
import com.mengze.domain.vo.SysUserInfoVo;
import com.mengze.service.SysUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

//个人信息接口
@RestController
@RequestMapping("/user")
@Api(tags = "用户", description = "用户信息相关接口")
public class SysUserController
{
    @Autowired
    private SysUserService sysUserService;

    //获取个人信息
    @GetMapping("/userInfo")
    @SystemLog(businessName = "查询用户信息")
    @ApiOperation(value = "获取用户信息", notes = "获取当前登录用户的个人信息", response = SysUserInfoVo.class)
    public ResponseResult userInfo()
    {
        return sysUserService.userInfo();
    }

    //更新个人信息
    @PutMapping("/userInfo")
    @SystemLog(businessName = "更新用户信息")
    @ApiOperation(value = "更新用户信息", notes = "更新当前登录用户的个人信息")
    public ResponseResult updateUserInfo(@Valid @RequestBody UpdateUserInfoDto dto)
    {
        return sysUserService.updateUserInfo(dto);
    }

    //用户注册
    @PostMapping("/userRegister")
    @SystemLog(businessName = "用户注册")
    @ApiOperation(value = "用户注册", notes = "新用户注册")
    public ResponseResult userRegister(@Valid @RequestBody UserRegisterDto dto)
    {
        return sysUserService.userRegister(dto);
    }

    //获取博主信息
    @GetMapping("/bloggerInfo")
    @SystemLog(businessName = "查询博主信息")
    @ApiOperation(value = "获取博主信息", notes = "获取博主的昵称和头像，用于在关于页面进行展示", response = BloggerInfoVo.class)
    public ResponseResult getBloggerInfo()
    {
        return sysUserService.getBloggerInfo();
    }

}
