package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.SendCodeDto;

public interface AuthService
{
    ResponseResult sendCode(SendCodeDto dto);
}
