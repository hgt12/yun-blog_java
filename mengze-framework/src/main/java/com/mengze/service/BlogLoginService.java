package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LoginDto;
import com.mengze.domain.dto.ResetPasswordDto;
import com.mengze.domain.dto.VerifyResetCodeDto;
import com.mengze.domain.entity.User;

public interface BlogLoginService
{

    ResponseResult login(User user);

    ResponseResult logout();

    ResponseResult loginByEmail(LoginDto dto);
    
    ResponseResult loginByPhone(LoginDto dto);

    ResponseResult verifyResetCode(VerifyResetCodeDto dto);

    ResponseResult resetPassword(ResetPasswordDto dto);
}
