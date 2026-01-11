package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.User;

public interface BlogLoginService
{

    ResponseResult login(User user);

    ResponseResult logout();
}
