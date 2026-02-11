package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "文章详情响应对象")
public class ArticleDetailVo
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
    
    @ApiModelProperty(value = "文章作者")
    private String author;
    
    @ApiModelProperty(value = "访问量")
    private Long viewCount;
    
    @ApiModelProperty(value = "字数统计")
    private Integer wordCount;
    
    @ApiModelProperty(value = "阅读时长（分钟）")
    private Integer readTime;
    
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "最后更新时间")
    private Date updateTime;

}
