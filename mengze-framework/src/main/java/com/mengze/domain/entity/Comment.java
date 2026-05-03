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
import java.io.Serializable;

/**
 * 评论表(Comment)表实体类
 *
 * @author makejava
 * @since 2026-01-12 15:48:50
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("comment")
public class Comment extends Model<Comment> {
    @TableId
    //评论ID
    private Long id;
    //评论类型（0代表文章评论，1代表友链评论，2代表留言板评论
    private String type;
    //文章ID
    private Long articleId;
    //评论用户ID（登录用户）
    private Long userId;
    //昵称
    private String nickname;
    //邮箱
    private String email;
    //个人网址
    private String personalWebsite;
    //头像URL
    private String avatar;
    //评论内容
    private String content;
    //父评论ID（回复时使用）
    private Long parentId;
    //根评论ID
    private Long rootId;
    //回复目标用户ID
    private Long replyToUserId;
    //回复目标评论的ID
    private Long replyToCommentId;
    //点赞数
    private Integer likeCount;
    //状态（0-正常，1-待审核，2-审核未通过）
    private String status;
    //删除标志（0-未删除，1-已删除）
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

