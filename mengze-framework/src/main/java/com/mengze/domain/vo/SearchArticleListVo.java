package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "搜索文章列表响应对象")
public class SearchArticleListVo
{
    @ApiModelProperty(value = "文章ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "文章标题", example = "Spring Boot入门教程")
    private String title;

    @ApiModelProperty(value = "文章摘要", example = "本文介绍Spring Boot的基础知识")
    private String summary;

    @ApiModelProperty(value = "文章内容", example = "Spring Boot是一个快速开发框架...")
    private String content;
}
