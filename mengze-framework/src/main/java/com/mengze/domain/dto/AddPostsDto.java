package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
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
    @NotBlank(message = "文章标题不能为空")
    @ApiModelProperty(value = "文章标题", required = true, example = "测试文章标题")
    private String title;

    @NotBlank(message = "文章内容不能为空")
    @ApiModelProperty(value = "文章内容（HTML格式/md格式）", required = true, example = "你好")
    private String content;

    @NotBlank(message = "文章内容类型不能为空")
    @Pattern(regexp = "^[01]$", message = "文章内容类型只能为0或1")
    @ApiModelProperty(value = "文章内容类型（0-html/富文本格式, 1-md格式）", required = true, example = "0")
    private String contentType;

    @ApiModelProperty(value = "文章摘要", example = "这是文章摘要")
    private String summary;

    @ApiModelProperty(value = "缩略图URL", example = "/thumbnails/vuejs.jpg")
    private String thumbnail;

    @NotNull(message = "分类ID不能为空")
    @ApiModelProperty(value = "分类ID", required = true, example = "1")
    private Long categoryId;

    @Pattern(regexp = "^[01]$", message = "是否置顶只能为0或1")
    @ApiModelProperty(value = "是否置顶（0-否，1-是）", example = "0")
    private String isTop;

    @NotNull(message = "文章状态不能为空")
    @Pattern(regexp = "^$|^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "状态（0-已发布，1-草稿）", required = true, example = "0")
    private String status;

    @Pattern(regexp = "^$|^[01]$", message = "是否允许评论只能为0或1")
    @ApiModelProperty(value = "是否允许评论（0-否，1-是）", example = "1")
    private String isComment;

    @ApiModelProperty(value = "标签列表", example = "[1,2,3]")
    private List<Long> tags;
}
