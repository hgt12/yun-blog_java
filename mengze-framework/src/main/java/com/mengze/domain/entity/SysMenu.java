package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单表(SysMenu)表实体类
 *
 * @author makejava
 * @since 2026-03-27 19:07:23
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_menu")
public class SysMenu extends Model<SysMenu> {
    @TableId

    private Long id;
    //父菜单ID
    private Long parentId;
    //菜单名称
    private String name;
    //菜单类型（C-目录 M-菜单 B-按钮）
    private String type;
    //路由路径
    private String path;
    //路由名称（前端路由名）
    private String routeName;
    //路由路径（可用于自定义路由路径）
    private String routePath;
    //跳转路径
    private String redirect;
    //组件路径
    private String component;
    //ICON
    private String icon;
    //排序
    private Integer sort;
    //菜单是否可见(0:显示;1:隐藏)
    private String visible;
    //权限标识
    private String perm;
    //目录是否始终显示(0:显示;1:隐藏)
    private String alwaysShow;
    //是否缓存(keepAlive)(0:缓存;1:不缓存)
    private String keepAlive;
    //路由参数
    private String params;
    //删除标记(0-未删除,1-已删除)
    private String delFlag;
    //创建者ID
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    //更新者ID
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}

