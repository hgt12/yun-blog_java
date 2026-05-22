package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 绑定或更换邮箱请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "绑定或更换邮箱请求对象")
public class UpdateOrBindEmailDto
{
    @NotBlank(message = "邮箱不能为空")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$", message = "邮箱格式不正确")
    @ApiModelProperty(value = "邮箱", required = true, example = "2962933152@qq.com")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
    @ApiModelProperty(value = "验证码", required = true, example = "123555")
    private String code;

    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "当前密码", required = true, example = "password123456")
    private String password;
}
