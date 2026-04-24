package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

//用户注册DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户注册请求对象")
public class UserRegisterDto {
    
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]{3,16}$", message = "用户名必须为2-16位字母、数字、下划线或中文")
    @ApiModelProperty(value = "用户名", required = true, example = "张三")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^.{6,20}$", message = "密码长度必须在6-20位之间")
    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;
    
    @Pattern(regexp = "^$|^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    @ApiModelProperty(value = "邮箱（邮箱注册时必填）", example = "user@example.com")
    private String email;
    
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @ApiModelProperty(value = "手机号（手机注册时必填）", example = "13800138000")
    private String phone;
    
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
    @ApiModelProperty(value = "验证码", required = true, example = "123456")
    private String code;

    @ApiModelProperty(value = "昵称", example = "张三")
    private String nickname;

}
