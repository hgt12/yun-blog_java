package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.SendCodeDto;

public interface AuthService
{
    ResponseResult sendEmailLoginCode(SendCodeDto dto);

    ResponseResult sendPhoneLoginCode(SendCodeDto dto);

    ResponseResult sendEmailRegisterCode(SendCodeDto dto);

    ResponseResult sendPhoneRegisterCode(SendCodeDto dto);

    ResponseResult sendEmailForgotPasswordCode(SendCodeDto dto);
}
