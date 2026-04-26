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
    
    @ApiModelProperty(value = "用户ID")
    private Long id;
    
    @ApiModelProperty(value = "昵称")
    private String nickname;
    
    @ApiModelProperty(value = "头像URL")
    private String avatar;
    
    @ApiModelProperty(value = "手机号")
    private String phone;
    
    @ApiModelProperty(value = "性别（0-男，1-女，2-隐藏）")
    private String sex;
    
    @ApiModelProperty(value = "邮箱")
    private String email;
    
    @ApiModelProperty(value = "用户角色列表")
    private List<String> roles;
}



