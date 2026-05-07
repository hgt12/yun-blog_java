package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色部门数据权限关联表(SysRoleDept)表实体类
 *
 * @author makejava
 * @since 2026-05-07 08:54:58
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_role_dept")
public class SysRoleDept extends Model<SysRoleDept> {
    @TableId
    //角色ID
    private Long roleId;
    //部门ID
    private Long deptId;
    //创建时间
    private Date createTime;

}

