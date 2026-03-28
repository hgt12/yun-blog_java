package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LoginDto;
import com.mengze.domain.dto.ResetPasswordDto;
import com.mengze.domain.dto.UserLoginDto;
import com.mengze.domain.dto.VerifyResetCodeDto;

public interface BlogLoginService
{

    ResponseResult login(UserLoginDto dto);

    ResponseResult logout();

    ResponseResult loginByEmail(LoginDto dto);
    
    ResponseResult loginByPhone(LoginDto dto);

    ResponseResult verifyResetCode(VerifyResetCodeDto dto);

    ResponseResult resetPassword(ResetPasswordDto dto);
}
