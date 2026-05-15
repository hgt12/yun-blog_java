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
@ApiModel(description = "后台分类表单详情响应对象")
public class CategoryFormDetailVo
{
    @ApiModelProperty(value = "分类ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "分类名称", example = "后端技术")
    private String name;

    @ApiModelProperty(value = "分类描述", example = "后端开发相关技术文章")
    private String description;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;
}
