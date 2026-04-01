package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户角色关联表(SysUserRole)表实体类
 *
 * @author makejava
 * @since 2026-03-27 19:28:41
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_user_role")
public class SysUserRole extends Model<SysUserRole> {
    @TableId
    //用户ID
    private Long userId;
    //角色ID
    private Long roleId;
    //创建时间
    private Date createTime;

}

