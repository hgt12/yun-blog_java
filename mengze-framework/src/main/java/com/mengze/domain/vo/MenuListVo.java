package com.mengze.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 菜单Vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "获取当前登录用户的菜单")
public class MenuListVo
{
    @ApiModelProperty(value = "菜单id", example = "6")
    private Long id;

    @ApiModelProperty(value = "父菜单ID", example = "1")
    private Long parentId;

    @ApiModelProperty(value = "菜单名称", example = "字典管理")
    private String name;

    @ApiModelProperty(value = "菜单类型（C-目录 M-菜单 B-按钮）", example = "M")
    private String type;

    @ApiModelProperty(value = "路由名称（前端路由名）", example = "Dict")
    private String routeName;

    @ApiModelProperty(value = "路由路径（可用于自定义路由字段）", example = "dict")
    private String routePath;

    @ApiModelProperty(value = "跳转路径", example = "/blog/list")
    private String redirect;

    @ApiModelProperty(value = "组件路径", example = "system/dict/index")
    private String component;

    @ApiModelProperty(value = "图标", example = "dict")
    private String icon;

    @ApiModelProperty(value = "排序", example = "5")
    private Integer sort;

    @ApiModelProperty(value = "菜单是否可见(1:显示;0:隐藏)", example = "0")
    private String visible;

    @ApiModelProperty(value = "权限标识", example = "sys:config:query")
    private String perm;

    @TableField(exist = false)
    @ApiModelProperty(value = "子菜单")
    private List<MenuListVo> children;
}
