package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 重置密码DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "重置密码请求对象")
public class ResetPasswordDto {
    
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @ApiModelProperty(value = "邮箱", required = true, example = "user@example.com")
    private String email;
    
    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{6}$", message = "验证码必须为6位数字")
    @ApiModelProperty(value = "验证码", required = true, example = "123456")
    private String code;
    
    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^.{6,20}$", message = "密码长度必须在6-20位之间")
    @ApiModelProperty(value = "新密码", required = true, example = "newPassword123")
    private String newPassword;
}
