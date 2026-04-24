package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AdminLoginDto;

public interface AdminAuthService
{

    ResponseResult login(AdminLoginDto dto);

    ResponseResult logout();

    ResponseResult captcha();

    ResponseResult refreshToken(String refreshToken);

}
