package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UserListDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.SysUserInfoVo;
import com.mengze.service.AdminUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/users")
@Api(tags = "用户信息", description = "后台用户信息相关接口")
public class AdminUserController
{
    @Autowired
    private AdminUserService adminUserService;

    @GetMapping("/info")
    @SystemLog(businessName = "获取后台用户信息")
    @ApiOperation(value = "获取后台用户信息", notes = "获取当前登录后台用户的个人信息", response = SysUserInfoVo.class)
    public ResponseResult getInfo()
    {
        return adminUserService.getAdminInfo();
    }

    @GetMapping
    @SystemLog(businessName = "分页查询用户列表")
    @ApiOperation(value = "获取用户列表", notes = "分页查询用户列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
    })
    public ResponseResult getUserList(Integer pageNum, Integer pageSize, @Valid UserListDto userListDto)
    {
        return adminUserService.getUserList(pageNum, pageSize, userListDto);
    }
}
