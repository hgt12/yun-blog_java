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
@ApiModel(description = "文章详情响应对象")
public class ArticleDetailVo
{
    @ApiModelProperty(value = "文章ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "文章标题", example = "Spring Boot入门教程")
    private String title;
    
    @ApiModelProperty(value = "文章摘要", example = "本文介绍Spring Boot的基础知识")
    private String summary;
    
    @ApiModelProperty(value = "所属分类ID", example = "1")
    private Long categoryId;
    
    @ApiModelProperty(value = "所属分类名", example = "后端技术")
    private String categoryName;
    
    @ApiModelProperty(value = "缩略图URL", example = "https://example.com/thumbnail.jpg")
    private String thumbnail;
    
    @ApiModelProperty(value = "文章内容", example = "Spring Boot是一个快速开发框架...")
    private String content;
    
    @ApiModelProperty(value = "文章作者", example = "梦泽")
    private String author;
    
    @ApiModelProperty(value = "访问量", example = "1000")
    private Long viewCount;
    
    @ApiModelProperty(value = "字数统计", example = "5000")
    private Integer wordCount;
    
    @ApiModelProperty(value = "阅读时长（分钟）", example = "10")
    private Integer readTime;
    
    @ApiModelProperty(value = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

    @ApiModelProperty(value = "最后更新时间", example = "2024-01-02 12:00:00")
    private Date updateTime;

    @ApiModelProperty(value = "标签列表")
    private List<TagVo> tags;

}
