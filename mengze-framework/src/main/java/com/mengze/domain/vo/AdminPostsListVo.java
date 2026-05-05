package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "后台文章列表响应对象")
public class AdminPostsListVo
{
    @ApiModelProperty(value = "文章ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "文章标题", example = "Spring Boot入门教程")
    private String title;

    @ApiModelProperty(value = "文章摘要", example = "本文介绍Spring Boot的基础知识")
    private String summary;

    @ApiModelProperty(value = "文章状态（0-已发布，1-草稿）", example = "0")
    private String status;

    @ApiModelProperty(value = "所属分类名", example = "后端技术")
    private String categoryName;

    @ApiModelProperty(value = "所属分类状态", example = "0")
    private String categoryStatus;

    @ApiModelProperty(value = "访问量", example = "1000")
    private Long viewCount;

    @ApiModelProperty(value = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

    @ApiModelProperty(value = "标签列表")
    private List<TagVo> tags;
}
