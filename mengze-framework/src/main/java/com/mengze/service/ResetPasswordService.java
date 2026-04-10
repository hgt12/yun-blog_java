package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.ResetPasswordDto;
import com.mengze.domain.dto.VerifyResetCodeDto;

/**
 * 公用重置密码接口
 */
public interface ResetPasswordService
{
    ResponseResult verifyResetCode(VerifyResetCodeDto dto);

    ResponseResult resetPassword(ResetPasswordDto dto);
}
