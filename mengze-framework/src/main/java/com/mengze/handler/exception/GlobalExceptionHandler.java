package com.mengze.handler.exception;

import com.mengze.domain.ResponseResult;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//全局异常处理类
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler
{
    @ExceptionHandler(SystemException.class)
    public ResponseResult systemExceptionHandler(SystemException exception)
    {
        //打印异常信息
        log.error("出现异常！！！ {}", exception);
        //从异常对象中获取异常提示信息并封装返回
        return ResponseResult.errorResult(exception.getCode(), exception.getMsg());
    }

    //对其他异常进行处理
    @ExceptionHandler(Exception.class)
    public ResponseResult exceptionHandler(Exception exception)
    {
        //打印异常信息
        log.error("出现异常！！！ {}", exception);
        //从异常对象中获取异常提示信息并封装返回
        return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), exception.getMessage());
    }
}
