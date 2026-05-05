package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 用户信息Vo
 */
@Data
@Accessors(chain = true)
@ApiModel(description = "后台用户信息响应对象")
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserInfoVo
{
    @ApiModelProperty(value = "用户信息")
    private SysUserInfoVo user;

    @ApiModelProperty(value = "角色列表", example = "[\"admin\", \"editor\"]")
    private List<String> roles;

    @ApiModelProperty(value = "菜单列表", example = "[\"system:user:list\", \"system:role:list\"]")
    private List<String> perms;

}
