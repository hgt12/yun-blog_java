package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UpdateUserInfoDto;
import com.mengze.domain.dto.UserRegisterDto;
import com.mengze.domain.entity.SysUser;

/**
 * 用户表(SysUser)表服务接口(前台)
 *
 * @author makejava
 * @since 2026-01-14 15:21:53
 */
public interface SysUserService extends IService<SysUser> {

    ResponseResult userInfo();

    ResponseResult updateUserInfo(UpdateUserInfoDto updateUserInfoDto);

    ResponseResult userRegister(UserRegisterDto dto);

    ResponseResult getBloggerInfo();
}

