package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文章列表操作请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "文章列表操作请求对象")
public class PostsListDto
{
    @ApiModelProperty(value = "文章ID（更新时必填）", example = "1")
    private Long id;

    @ApiModelProperty(value = "分类id", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "标签id", example = "1")
    private Long tagId;

    @ApiModelProperty(value = "文章状态（已发布，草稿）", example = "草稿")
    private String status;

    @ApiModelProperty(value = "文章ID列表", required = true, example = "[1,2,3]")
    private List<Long> ids;

    @ApiModelProperty(value = "关键字（用于模糊搜索文章标题）", example = "标题")
    private String keywords;
}
