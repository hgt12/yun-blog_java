package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 绑定或更换手机号请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "绑定或更换手机号请求对象")
public class UpdateOrBindPhoneDto
{
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @ApiModelProperty(value = "手机号", required = true, example = "123456788433")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
    @ApiModelProperty(value = "验证码", required = true, example = "123555")
    private String code;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "当前密码", required = true, example = "password123456")
    private String password;
}
