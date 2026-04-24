package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户名密码登录DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户名密码登录请求对象")
public class AdminLoginDto {
    
    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty(value = "用户名", required = true, example = "zhangsan")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;

    @ApiModelProperty(value = "验证码ID", example = "1f43e2")
    private String captchaId;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^[A-Za-z0-9]{4}$", message = "验证码必须为4位字母或数字")
    @ApiModelProperty(value = "验证码", required = true, example = "HU3H")
    private String captchaCode;
}
