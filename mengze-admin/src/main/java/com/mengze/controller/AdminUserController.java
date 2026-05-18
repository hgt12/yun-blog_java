package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddUserDto;
import com.mengze.domain.dto.UpdateUserDto;
import com.mengze.domain.dto.UserListDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.SysUserInfoVo;
import com.mengze.domain.vo.UserFormDetailVo;
import com.mengze.service.AdminUserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{userId}/form")
    @SystemLog(businessName = "获取用户表单数据")
    @ApiOperation(value = "获取用户表单数据", notes = "获取用户表单数据，用于编辑用户详情", response = UserFormDetailVo.class)
    @ApiImplicitParam(name = "userId", value = "用户id", dataType = "long", paramType = "path")
    public ResponseResult<UserFormDetailVo> getUserFormDetail(@PathVariable Long userId)
    {
        return adminUserService.getUserFormDetail(userId);
    }

    @PostMapping
    @SystemLog(businessName = "新增用户")
    @ApiOperation(value = "新增用户接口", notes = "新增用户", response = ResponseResult.class)
    public ResponseResult addUser(@Valid @RequestBody AddUserDto addUserDto)
    {
        return adminUserService.addUser(addUserDto);
    }

    @PutMapping("/{id}")
    @SystemLog(businessName = "修改用户信息")
    @ApiOperation(value = "修改用户接口", notes = "修改用户信息", response = String.class)
    @ApiImplicitParam(name = "id", value = "用户ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserDto updateUserDto)
    {
        return adminUserService.updateUser(id, updateUserDto);
    }
}
