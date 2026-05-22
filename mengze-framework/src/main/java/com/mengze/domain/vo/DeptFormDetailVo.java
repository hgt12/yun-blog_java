package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "部门表单详情响应对象")
public class DeptFormDetailVo
{
    @ApiModelProperty(value = "部门ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "部门名称", example = "步步部")
    private String name;

    @ApiModelProperty(value = "部门编号", example = "ROOT")
    private String code;

    @ApiModelProperty(value = "父部门ID", example = "1")
    private Long parentId;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;
}
