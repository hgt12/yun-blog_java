package com.mengze.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "菜单ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "父菜单ID", example = "0")
    private Long parentId;
    
    @ApiModelProperty(value = "菜单名称", example = "系统管理")
    private String name;
    
    @ApiModelProperty(value = "菜单类型（C-目录 M-菜单 B-按钮）", example = "C")
    private String type;
    
    @ApiModelProperty(value = "路由字符串", example = "/system")
    private String path;
    
    @ApiModelProperty(value = "路由名称（前端路由名）", example = "System")
    private String routeName;
    
    @ApiModelProperty(value = "跳转路径", example = "/system/user")
    private String redirect;
    
    @ApiModelProperty(value = "组件路径", example = "system/user/index")
    private String component;
    
    @ApiModelProperty(value = "图标", example = "system")
    private String icon;

    @TableField(exist = false)
    @ApiModelProperty(value = "子菜单列表")
    private List<MenuRoutesVo> children;
}
