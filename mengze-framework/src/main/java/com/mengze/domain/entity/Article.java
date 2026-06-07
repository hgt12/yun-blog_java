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
 * 文章表(Article)表实体类
 *
 * @author makejava
 * @since 2026-01-07 13:22:33
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("article")
public class Article extends Model<Article> {
    @TableId
    //文章ID
    private Long id;
    //文章标题
    private String title;
    //文章内容
    private String content;
    //文章内容类型（0-html/富文本格式, 1-md格式）
    private String contentType;
    //文章摘要
    private String summary;
    //缩略图URL
    private String thumbnail;
    //分类ID
    private Long categoryId;
    //分类名(因为这个字段在数据库中不存在，使用 @TableField(exist = false) 来标注出来)
    @TableField(exist = false)
    private String categoryName;
    //分类状态(非数据库字段)
    @TableField(exist = false)
    private String categoryStatus;
    //浏览量
    private Long viewCount;
    //是否置顶（0-否，1-是）
    private String isTop;
    //状态（0-已发布，1-草稿）
    private String status;
    //是否允许评论（0-否，1-是）
    private String isComment;
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

    public Article(Long id, long viewCount)
    {
        this.id = id;
        this.viewCount = viewCount;
    }
}

