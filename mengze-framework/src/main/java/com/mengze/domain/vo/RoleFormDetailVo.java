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
@ApiModel(description = "后台角色表单详情响应对象")
public class RoleFormDetailVo
{
    @ApiModelProperty(value = "角色ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "角色名称", example = "系统管理员")
    private String name;

    @ApiModelProperty(value = "角色编码", example = "admin")
    private String roleCode;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer dataScope;

    @ApiModelProperty(value = "部门id列表", example = "1, 2, 3")
    private List<Long> deptIds;

    @ApiModelProperty(value = "备注", example = "系统管理员，拥有所有权限")
    private String remark;
}
