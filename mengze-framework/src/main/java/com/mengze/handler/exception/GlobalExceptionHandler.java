package com.mengze.handler.exception;

import com.mengze.domain.ResponseResult;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    //处理 @RequestBody 参数校验异常（@Valid）
    //当使用 @Valid 注解校验 @RequestBody 参数时抛出
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseResult handleMethodArgumentNotValidException(MethodArgumentNotValidException exception)
    {
        //获取第一个校验失败的字段错误信息
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "参数校验失败";
        
        log.error("参数校验失败：{}", errorMessage);
        return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), errorMessage);
    }

    //处理 @Valid 校验异常（用于表单参数、查询参数）
    //当使用 @Valid 注解校验非 @RequestBody 参数时抛出
    @ExceptionHandler(BindException.class)
    public ResponseResult handleBindException(BindException exception)
    {
        //获取第一个校验失败的字段错误信息
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String errorMessage = fieldError != null ? fieldError.getDefaultMessage() : "参数绑定失败";
        
        log.error("参数绑定校验失败：{}", errorMessage);
        return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), errorMessage);
    }


    //处理单个参数校验异常（@Validated）
    //当在类上使用 @Validated 注解，对方法参数进行校验时抛出
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseResult handleConstraintViolationException(ConstraintViolationException exception)
    {
        //获取所有校验失败的信息
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        String errorMessage = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        
        if (errorMessage.isEmpty()) {
            errorMessage = "参数校验失败";
        }
        
        log.error("约束校验失败：{}", errorMessage);
        return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), errorMessage);
    }

    //处理文件上传大小超限异常
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseResult maxUploadSizeExceededExceptionHandler(MaxUploadSizeExceededException exception)
    {
        //打印异常信息
        log.error("文件上传大小超限！异常详情：{}", exception.getMessage());
        return ResponseResult.errorResult(AppHttpCodeEnum.FILE_SIZE_ERROR);
    }

    //处理数据库重复数据冲突
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseResult<Void> handleDuplicateKeyException(DuplicateKeyException e)
    {
        log.error("数据库数据重复！！！", e);

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
                } else if (keyName.contains("uk_address") || keyName.contains("address")) {
                    message = "站点地址 '" + duplicateValue + "' 已存在！";
                } else {
                    // 通用处理：提取字段名
                    message = "数据 '" + duplicateValue + "' 已存在，请勿重复添加！";
                }
            }
        }

        return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), message);
    }

    //处理登录认证失败异常（用户名或密码错误）
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseResult handleBadCredentialsException(BadCredentialsException exception)
    {
        //输出日志
        log.warn("用户登录认证失败：用户名或密码错误");

        return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_ERROR.getCode(), "用户名或密码错误");
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
