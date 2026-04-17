package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//用户注册DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户注册请求对象")
public class UserRegisterDto {
    
    @ApiModelProperty(value = "用户名", required = true, example = "zhangsan")
    private String username;
    
    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;
    
    @ApiModelProperty(value = "邮箱（邮箱注册时必填）", example = "user@example.com")
    private String email;
    
    @ApiModelProperty(value = "手机号（手机注册时必填）", example = "13800138000")
    private String phone;
    
    @ApiModelProperty(value = "验证码", required = true, example = "123456")
    private String code;

    @ApiModelProperty(value = "昵称", example = "张三")
    private String nickname;

}
