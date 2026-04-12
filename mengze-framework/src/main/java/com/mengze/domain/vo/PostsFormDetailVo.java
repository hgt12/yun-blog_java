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
    @ApiModelProperty(value = "文章ID")
    private Long id;

    @ApiModelProperty(value = "文章标题")
    private String title;

    @ApiModelProperty(value = "文章摘要")
    private String summary;

    @ApiModelProperty(value = "所属分类ID")
    private Long categoryId;

    @ApiModelProperty(value = "所属分类名")
    private String categoryName;

    @ApiModelProperty(value = "缩略图URL")
    private String thumbnail;

    @ApiModelProperty(value = "文章内容")
    private String content;

    @ApiModelProperty(value = "标签ID列表")
    private List<Long> tags;

    @ApiModelProperty(value = "状态（0-已发布，1-草稿）")
    private String status;

    @ApiModelProperty(value = "是否允许评论（0-否，1-是）")
    private String isComment;

    @ApiModelProperty(value = "是否置顶（0-否，1-是）")
    private String isTop;
}
