package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    private String email;
    
    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;
    
    @ApiModelProperty(value = "用户性别（0-男，1-女，2-隐藏）", example = "0")
    private String sex;
    
    @ApiModelProperty(value = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
}
