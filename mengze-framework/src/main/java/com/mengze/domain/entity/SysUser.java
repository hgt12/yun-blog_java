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
 * 用户表(User)表实体类
 *
 * @author makejava
 * @since 2026-01-07 15:53:34
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_user")
public class SysUser extends Model<SysUser> {
    @TableId
    //用户ID
    private Long id;
    //用户名
    private String username;
    //密码（加密存储）
    private String password;
    //昵称
    private String nickname;
    //邮箱
    private String email;
    //用户类型（0 代表普通用户，1 代表管理员）
    private String userType;
    //账号状态（0 正常，1 停用）
    private String status;
    //手机号
    private String phone;
    //用户性别，（0 男，1 女，2 隐藏）
    private String sex;
    //头像URL
    private String avatar;
    //删除标志（0-未删除，1-已删除）
    private String delFlag;
    //创建人的用户id
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
    //最后登录时间
    private Date lastLoginTime;

}

