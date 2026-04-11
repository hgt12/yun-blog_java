package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 文章列表操作请求DTO
 */
@Data
@ApiModel(description = "文章列表操作请求对象")
public class PostsListDto
{
    @ApiModelProperty(value = "文章ID（更新时必填）", example = "1")
    private Long id;

    @ApiModelProperty(value = "文章标题", required = true, example = "测试标题")
    private String title;

    @ApiModelProperty(value = "分类id", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "文章状态（已发布，草稿）", example = "草稿")
    private String status;

    @ApiModelProperty(value = "文章ID列表", required = true, example = "[1,2,3]")
    private List<Long> ids;

    @ApiModelProperty(value = "关键字（用于模糊搜索文章标题）", example = "标题")
    private String keywords;
}
