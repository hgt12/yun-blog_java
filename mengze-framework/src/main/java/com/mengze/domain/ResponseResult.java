package com.mengze.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.mengze.enums.AppHttpCodeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

//统一响应类
@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "统一响应结果")
public class ResponseResult<T> implements Serializable {
    
    @ApiModelProperty(value = "响应码（200-成功，其他-失败）")
    private Integer code;
    
    @ApiModelProperty(value = "响应消息")
    private String msg;
    
    @ApiModelProperty(value = "响应数据")
    private T data;

    public ResponseResult() {
        this.code = AppHttpCodeEnum.SUCCESS.getCode();
        this.msg = AppHttpCodeEnum.SUCCESS.getMsg();
    }

    public ResponseResult(Integer code, T data) {
        this.code = code;
        this.data = data;
    }

    public ResponseResult(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public ResponseResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    //用于返回成功结果，可以附带数据或自定义提示。
    public static ResponseResult okResult() {
        ResponseResult result = new ResponseResult();
        return result;
    }

    public static ResponseResult okResult(int code, String msg) {
        ResponseResult result = new ResponseResult();
        return result.ok(code, null, msg);
    }

    public static ResponseResult okResult(Object data) {
        ResponseResult result = setAppHttpCodeEnum(AppHttpCodeEnum.SUCCESS,
                AppHttpCodeEnum.SUCCESS.getMsg());
        if (data != null) {
            result.setData(data);
        }
        return result;
    }

    //用于返回错误结果，可以直接用枚举类里的错误码和提示。
    public static ResponseResult errorResult(int code, String msg) {
        ResponseResult result = new ResponseResult();
        return result.error(code, msg);
    }

    public static ResponseResult errorResult(AppHttpCodeEnum enums) {
        return setAppHttpCodeEnum(enums, enums.getMsg());
    }

    public static ResponseResult errorResult(AppHttpCodeEnum enums, String msg) {
        return setAppHttpCodeEnum(enums, msg);
    }

    //根据枚举类快速生成响应对象
    public static ResponseResult setAppHttpCodeEnum(AppHttpCodeEnum enums) {
        return okResult(enums.getCode(), enums.getMsg());
    }

    private static ResponseResult setAppHttpCodeEnum(AppHttpCodeEnum enums, String msg) {
        return okResult(enums.getCode(), msg);
    }

    public ResponseResult<?> error(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
        return this;
    }

    public ResponseResult<?> ok(Integer code, T data) {
        this.code = code;
        this.data = data;
        return this;
    }

    public ResponseResult<?> ok(Integer code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
        return this;
    }

    public ResponseResult<?> ok(T data) {
        this.data = data;
        return this;
    }

}