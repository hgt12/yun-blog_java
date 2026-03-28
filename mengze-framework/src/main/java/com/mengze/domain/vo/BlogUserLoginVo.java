package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户登录响应对象")
public class BlogUserLoginVo
{
    @ApiModelProperty(value = "JWT令牌")
    private String token;
    
    @ApiModelProperty(value = "用户信息")
    private SysUserInfoVo userInfo;
}
