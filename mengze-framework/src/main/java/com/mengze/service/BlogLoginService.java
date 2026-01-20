package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LoginDto;
import com.mengze.domain.entity.User;

public interface BlogLoginService
{

    ResponseResult login(User user);

    ResponseResult logout();

    ResponseResult loginByEmail(LoginDto dto);
    
    ResponseResult loginByPhone(LoginDto dto);
}
