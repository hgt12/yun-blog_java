package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 验证重置验证码DTO
 */
@Data
@ApiModel(description = "验证重置验证码请求对象")
public class VerifyResetCodeDto {
    
    @ApiModelProperty(value = "邮箱", required = true, example = "user@example.com")
    private String email;
    
    @ApiModelProperty(value = "验证码", required = true, example = "123456")
    private String code;
}
