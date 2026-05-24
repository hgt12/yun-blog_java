package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

/**
 * 文章列表操作请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "文章列表操作请求对象")
public class PostsListDto
{

    @ApiModelProperty(value = "分类id", example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "标签id", example = "1")
    private Long tagId;

    @Pattern(regexp = "^$|^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "文章状态（0-已发布，1-草稿）", example = "0")
    private String status;

    @ApiModelProperty(value = "关键字（文章标题或摘要）", example = "标题")
    private String keywords;
}
