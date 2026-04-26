package com.mengze.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 菜单Vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "获取当前登录用户的菜单")
public class MenuRoutesVo
{
    @TableId
    private Long id;
    //父菜单ID
    private Long parentId;
    //菜单名称
    private String name;
    //菜单类型（C-目录 M-菜单 B-按钮）
    private String type;
    //路由字符串
    private String path;
    //路由名称（前端路由名）
    private String routeName;
    //跳转路径
    private String redirect;
    //组件路径
    private String component;
    //ICON
    private String icon;

    @TableField(exist = false)
    private List<MenuRoutesVo> children;
}
