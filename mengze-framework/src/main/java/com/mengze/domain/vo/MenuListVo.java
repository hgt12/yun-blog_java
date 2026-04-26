package com.mengze.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
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
    @TableId
    private Long id;
    //父菜单ID
    private Long parentId;
    //菜单名称
    private String name;
    //菜单类型（C-目录 M-菜单 B-按钮）
    private String type;
    //路由名称（前端路由名）
    private String routeName;
    //路由路径（可用于自定义路由字段）
    private String routePath;
    //跳转路径
    private String redirect;
    //组件路径
    private String component;
    //ICON
    private String icon;
    //排序
    private Integer sort;
    //菜单是否可见(1:显示;0:隐藏)
    private String visible;
    //菜单范围(1=平台 2=业务)
    private String scope;
    //权限标识
    private String perm;

    @TableField(exist = false)
    private List<MenuListVo> children;
}
