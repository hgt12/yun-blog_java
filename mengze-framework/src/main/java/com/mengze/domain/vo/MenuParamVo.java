package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "菜单路由参数响应对象")
public class MenuParamVo
{
    @ApiModelProperty(value = "参数名称", example = "from")
    private String key;

    @ApiModelProperty(value = "参数值", example = "admin")
    private String value;
}
