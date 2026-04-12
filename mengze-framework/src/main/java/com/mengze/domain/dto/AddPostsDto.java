package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 添加文章请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "添加文章请求对象")
public class AddPostsDto
{
    @ApiModelProperty(value = "文章ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "文章标题", required = true, example = "测试文章标题")
    private String title;

    @ApiModelProperty(value = "文章内容（HTML格式/md格式）", required = true, example = "你好")
    private String content;

    @ApiModelProperty(value = "文章摘要", example = "这是文章摘要")
    private String summary;

    @ApiModelProperty(value = "缩略图URL", example = "/thumbnails/vuejs.jpg")
    private String thumbnail;

    @ApiModelProperty(value = "分类ID", required = true, example = "1")
    private Long categoryId;

    @ApiModelProperty(value = "是否置顶（0-否，1-是）", required = true, example = "0")
    private String isTop;

    @ApiModelProperty(value = "状态（0-已发布，1-草稿）", example = "0")
    private String status;

    @ApiModelProperty(value = "是否允许评论（0-否，1-是）", example = "1")
    private String isComment;

    @ApiModelProperty(value = "标签列表")
    private List<Long> tags;
}
