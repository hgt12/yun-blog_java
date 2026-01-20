package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UserRegisterDto;
import com.mengze.domain.entity.User;

/**
 * 用户表(User)表服务接口
 *
 * @author makejava
 * @since 2026-01-14 15:21:53
 */
public interface UserService extends IService<User> {

    ResponseResult userInfo();

    ResponseResult updateUserInfo(User user);

    ResponseResult userRegister(UserRegisterDto dto);
}

