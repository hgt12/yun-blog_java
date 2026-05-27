package com.mengze.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * 系统通知公告表(SysNotice)表实体类
 *
 * @author makejava
 * @since 2026-05-25 20:36:02
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_notice")
public class SysNotice extends Model<SysNotice> {
    @TableId

    private Long id;
    //通知标题
    private String title;
    //通知内容
    private String content;
    //通知类型（关联字典编码：notice_type）
    private Integer type;
    //通知等级（字典code：notice_level）
    private String level;
    //目标类型（1: 全体, 2: 指定）
    private String targetType;
    //目标人ID集合（多个使用英文逗号,分割）
    private String targetUserIds;
    //发布人ID
    private Long publisherId;
    //状态（0: 未发布, 1: 已发布, 2: 已撤回）
    private String status;
    //是否删除（0: 未删除, 1: 已删除）
    private String delFlag;
    //发布时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date publishTime;
    //撤回时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date revokeTime;
    //创建人ID
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    //更新人ID
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

}

