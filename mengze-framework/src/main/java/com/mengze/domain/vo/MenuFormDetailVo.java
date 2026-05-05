package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "菜单详情响应对象")
public class MenuFormDetailVo
{
    @ApiModelProperty(value = "菜单ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "父菜单ID", example = "0")
    private Long parentId;

    @ApiModelProperty(value = "菜单名称", example = "字典管理")
    private String name;

    @ApiModelProperty(value = "菜单类型（C-目录 M-菜单 B-按钮）", example = "M")
    private String type;

    @ApiModelProperty(value = "路由字符串", example = "dict")
    private String path;

    @ApiModelProperty(value = "路由名称（前端路由名）", example = "Dict")
    private String routeName;

    @ApiModelProperty(value = "路由路径（可用于自定义路由字段）", example = "dict")
    private String routePath;

    @ApiModelProperty(value = "跳转路径", example = "/system/dict")
    private String redirect;

    @ApiModelProperty(value = "组件路径", example = "system/dict/index")
    private String component;

    @ApiModelProperty(value = "ICON", example = "dict")
    private String icon;

    @ApiModelProperty(value = "排序", example = "5")
    private Integer sort;

    @ApiModelProperty(value = "菜单是否可见(0:显示;1:隐藏)", example = "0")
    private String visible;

    @ApiModelProperty(value = "权限标识", example = "sys:config:query")
    private String perm;

    @ApiModelProperty(value = "目录是否始终显示", example = "false")
    private Boolean alwaysShow;

    @ApiModelProperty(value = "是否缓存(keepAlive)", example = "true")
    private Boolean keepAlive;

    @ApiModelProperty(value = "路由参数")
    private List<MenuParamVo> params;

}
