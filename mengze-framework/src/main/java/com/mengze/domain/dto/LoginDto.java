package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//登录请求DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "登录请求对象")
public class LoginDto {
    
    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;

    @ApiModelProperty(value = "邮箱地址", example = "user@example.com")
    private String email;
    
    @ApiModelProperty(value = "验证码", required = true, example = "123456")
    private String code;
}
