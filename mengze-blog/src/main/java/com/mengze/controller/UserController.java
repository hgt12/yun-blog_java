package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UpdateUserInfoDto;
import com.mengze.domain.dto.UserRegisterDto;
import com.mengze.domain.entity.User;
import com.mengze.domain.vo.UserInfoVo;
import com.mengze.service.UserService;
import com.mengze.utils.BeanCopyUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//个人信息接口
@RestController
@RequestMapping("/user")
@Api(tags = "用户", description = "用户信息相关接口")
public class UserController
{
    @Autowired
    private UserService userService;

    //获取个人信息
    @GetMapping("/userInfo")
    @SystemLog(businessName = "查询用户信息")
    @ApiOperation(value = "获取用户信息", notes = "获取当前登录用户的个人信息", response = UserInfoVo.class)
    public ResponseResult userInfo()
    {
        return userService.userInfo();
    }

    //更新个人信息
    @PutMapping("/userInfo")
    @SystemLog(businessName = "更新用户信息")
    @ApiOperation(value = "更新用户信息", notes = "更新当前登录用户的个人信息")
    public ResponseResult updateUserInfo(@RequestBody UpdateUserInfoDto dto)
    {
        User user = BeanCopyUtils.copyBean(dto, User.class);
        return userService.updateUserInfo(user);
    }

    //用户注册
    @PostMapping("/userRegister")
    @SystemLog(businessName = "用户注册")
    @ApiOperation(value = "用户注册", notes = "新用户注册")
    public ResponseResult userRegister(@RequestBody UserRegisterDto dto)
    {
        return userService.userRegister(dto);
    }

}
