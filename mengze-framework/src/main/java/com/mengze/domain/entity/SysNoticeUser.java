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
 * 用户通知公告关联表(SysNoticeUser)表实体类
 *
 * @author makejava
 * @since 2026-05-25 20:36:08
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_notice_user")
public class SysNoticeUser extends Model<SysNoticeUser> {
    @TableId
    //id
    private Long id;
    //公共通知id
    private Long noticeId;
    //用户id
    private Long userId;
    //读取状态（0: 未读, 1: 已读）
    private String isRead;
    //逻辑删除(0: 未删除, 1: 已删除)
    private String delFlag;
    //阅读时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date readTime;
    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}

