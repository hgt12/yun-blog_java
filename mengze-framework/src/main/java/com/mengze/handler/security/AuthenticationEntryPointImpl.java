package com.mengze.handler.security;

import com.alibaba.fastjson.JSON;
import com.mengze.domain.ResponseResult;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.utils.WebUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//认证失败处理器
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint
{

    @Override
    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException
    {
        e.printStackTrace();//打印异常信息
        //.InsufficientAuthenticationException，权限不足，或需要登录
        //.BadCredentialsException，用户名或密码错误
        ResponseResult result = null;
        if(e instanceof BadCredentialsException){
            result = ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_ERROR.getCode(), e.getMessage());//登录错误
        }else if(e instanceof InsufficientAuthenticationException){
            result = ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);//权限不足，或需要登录
        }else {
            result = ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), "认证或授权失败");
    }
        //响应给前端
        WebUtils.renderString(httpServletResponse, JSON.toJSONString(result));
    }
}
