package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

/**
 * 更新用户信息DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "更新用户信息请求对象")
public class UpdateUserInfoDto {
    
    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    private Long id;
    
    @ApiModelProperty(value = "昵称", example = "张三")
    private String nickname;
    
    @Pattern(regexp = "^$|^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    private String email;
    
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;
    
    @Pattern(regexp = "^$|^[012]$", message = "性别只能为0、1或2")
    @ApiModelProperty(value = "用户性别（0-男，1-女，2-隐藏）", example = "0")
    private String sex;
    
    @ApiModelProperty(value = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
}
