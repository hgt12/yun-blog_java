package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "热门文章响应对象")
public class HotArticleListVo
{
    @ApiModelProperty(value = "文章ID")
    private Long id;
    
    @ApiModelProperty(value = "文章标题")
    private String title;
    
    @ApiModelProperty(value = "访问量")
    private Long viewCount;
}
