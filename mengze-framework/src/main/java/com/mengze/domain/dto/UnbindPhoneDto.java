package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 解绑手机号操作请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "解绑手机号操作请求对象")
public class UnbindPhoneDto
{
    @NotBlank(message = "密码不能为空")
    @ApiModelProperty(value = "密码", example = "password123456")
    private String password;
}
