package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "后台标签表单详情响应对象")
public class TagFormDetailVo
{
    @ApiModelProperty(value = "标签ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "标签名称", example = "Java")
    private String name;

    @ApiModelProperty(value = "备注", example = "Java相关技术文章")
    private String remark;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;
}
