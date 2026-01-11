package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.User;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import com.mengze.service.BlogLoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

//登录接口
@RestController
public class BlogLoginController
{
    @Autowired
    private BlogLoginService blogLoginService;

    @PostMapping("/login")
    public ResponseResult login(@RequestBody User user)
    {
        //预防编程，用户名是否为空
        if (!StringUtils.hasText(user.getUsername()))
        {
            // 提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);//需要用户名
        }
        return blogLoginService.login(user);
    }

}
