package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "后台文章表单详情响应对象")
public class PostsFormDetailVo
{
    @ApiModelProperty(value = "文章ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "文章标题", example = "Spring Boot入门教程")
    private String title;

    @ApiModelProperty(value = "文章摘要", example = "本文介绍Spring Boot的基础知识")
    private String summary;

    @ApiModelProperty(value = "文章内容类型（0-html/富文本格式, 1-md格式）", example = "0")
    private String contentType;

    @ApiModelProperty(value = "所属分类ID", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "所属分类名", example = "后端技术")
    private String categoryName;

    @ApiModelProperty(value = "缩略图URL", example = "https://example.com/thumbnail.jpg")
    private String thumbnail;

    @ApiModelProperty(value = "文章内容", example = "Spring Boot是一个快速开发框架...")
    private String content;

    @ApiModelProperty(value = "标签ID列表", example = "[1, 2, 3]")
    private List<Long> tags;

    @ApiModelProperty(value = "状态（0-已发布，1-草稿）", example = "0")
    private String status;

    @ApiModelProperty(value = "是否允许评论（0-否，1-是）", example = "1")
    private String isComment;

    @ApiModelProperty(value = "是否置顶（0-否，1-是）", example = "0")
    private String isTop;
}
