package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 重置密码DTO
 */
@Data
@ApiModel(description = "重置密码请求对象")
public class ResetPasswordDto {
    
    @ApiModelProperty(value = "邮箱", required = true, example = "user@example.com")
    private String email;
    
    @ApiModelProperty(value = "验证码", required = true, example = "123456")
    private String code;
    
    @ApiModelProperty(value = "新密码", required = true, example = "newPassword123")
    private String newPassword;
}
