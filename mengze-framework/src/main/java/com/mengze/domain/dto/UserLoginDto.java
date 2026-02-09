package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 用户名密码登录DTO
 */
@Data
@ApiModel(description = "用户名密码登录请求对象")
public class UserLoginDto {
    
    @ApiModelProperty(value = "用户名", required = true, example = "zhangsan")
    private String username;
    
    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;
}
