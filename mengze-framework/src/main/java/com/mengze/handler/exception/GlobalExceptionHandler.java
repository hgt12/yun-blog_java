package com.mengze.handler.exception;

import com.mengze.domain.ResponseResult;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    //处理文件上传大小超限异常
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseResult maxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException exception)
    {
        //打印异常信息
        log.error("文件上传大小超限！异常详情：{}", exception.getMessage());
        return ResponseResult.errorResult(AppHttpCodeEnum.FILE_SIZE_ERROR);
    }

    //处理数据库唯一键值冲突
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseResult<Void> handleDuplicateKeyException(DuplicateKeyException e)
    {
        log.error("数据库唯一键冲突", e);

        String errorMsg = e.getCause().getMessage();
        String message = "数据已存在，请检查后重试";

        // 解析具体的重复字段
        if (errorMsg.contains("Duplicate entry")) {
            // 提取重复的值
            Pattern pattern = Pattern.compile("Duplicate entry '(.+?)' for key '(.+?)'");
            Matcher matcher = pattern.matcher(errorMsg);

            if (matcher.find()) {
                String duplicateValue = matcher.group(1);
                String keyName = matcher.group(2);

                // 根据键名返回提示
                if (keyName.contains("uk_name") || keyName.contains("name")) {
                    message = "名称 '" + duplicateValue + "' 已存在！";
                } else if (keyName.contains("uk_username") || keyName.contains("username")) {
                    message = "用户名 '" + duplicateValue + "' 已存在！";
                }
            }
        }

        return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), message);
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
