package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LoginByEmailOrPhoneDto;
import com.mengze.domain.dto.LoginDto;

public interface BlogLoginService
{

    ResponseResult login(LoginDto dto);

    ResponseResult logout();

    ResponseResult loginByEmail(LoginByEmailOrPhoneDto dto);
    
    ResponseResult loginByPhone(LoginByEmailOrPhoneDto dto);

}
