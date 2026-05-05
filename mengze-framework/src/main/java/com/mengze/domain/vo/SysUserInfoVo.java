package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel(description = "前台用户信息响应对象")
public class SysUserInfoVo {
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "昵称", example = "张三")
    private String nickname;
    
    @ApiModelProperty(value = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatar;
    
    @ApiModelProperty(value = "手机号", example = "13800138000")
    private String phone;
    
    @ApiModelProperty(value = "性别（0-男，1-女，2-隐藏）", example = "0")
    private String sex;
    
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    private String email;
    
    @ApiModelProperty(value = "用户角色列表", example = "[\"admin\", \"user\"]")
    private List<String> roles;
}



