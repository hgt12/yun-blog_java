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
 * 角色菜单关联表(SysRoleMenu)表实体类
 *
 * @author makejava
 * @since 2026-03-27 19:28:57
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_role_menu")
public class SysRoleMenu extends Model<SysRoleMenu> {
    @TableId
    //角色ID
    private Long roleId;
    //菜单ID
    private Long menuId;
    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

}

