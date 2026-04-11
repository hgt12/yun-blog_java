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
    @ApiModelProperty(value = "文章ID")
    private Long id;

    @ApiModelProperty(value = "文章标题")
    private String title;

    @ApiModelProperty(value = "文章状态（0-已发布，1-草稿）")
    private String status;

    @ApiModelProperty(value = "所属分类名")
    private String categoryName;

    @ApiModelProperty(value = "访问量")
    private Long viewCount;

    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "标签列表")
    private List<TagVo> tags;
}
