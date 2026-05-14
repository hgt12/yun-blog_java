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
@ApiModel(description = "角色响应对象")
public class AdminRoleListVo
{
    @ApiModelProperty(value = "角色ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "角色编码", example = "admin")
    private String roleCode;

    @ApiModelProperty(value = "角色名称", example = "系统管理员")
    private String name;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "数据权限(1-所有数据 2-部门及子部门数据 3-本部门数据 4-本人数据 5-自定义部门数据)", example = "1")
    private String dataScope;

    @ApiModelProperty(value = "创建日期", example = "2024-01-01 12:00:00")
    private Date createTime;

    @ApiModelProperty(value = "更新日期", example = "2024-01-01 12:00:00")
    private Date updateTime;
}
