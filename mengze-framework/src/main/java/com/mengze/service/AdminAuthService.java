package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UserLoginDto;

public interface AdminAuthService
{

    ResponseResult login(UserLoginDto dto);

    ResponseResult captcha(UserLoginDto dto);

}
