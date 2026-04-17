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
@ApiModel(description = "分类响应对象")
public class AdminCategoryListVo
{
    @ApiModelProperty(value = "分类ID")
    private Long id;

    @ApiModelProperty(value = "分类名")
    private String name;

    @ApiModelProperty(value = "分类描述")
    private String description;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）")
    private String status;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "文章数量")
    private Integer articleCount;

    @ApiModelProperty(value = "创建日期")
    private Date createTime;
}
