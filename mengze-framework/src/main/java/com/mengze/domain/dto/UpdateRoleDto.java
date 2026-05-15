package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 编辑角色请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "编辑角色请求对象")
public class UpdateRoleDto
{

    @NotBlank(message = "角色编码不能为空")
    @ApiModelProperty(value = "角色编码", required = true, example = "testRole")
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @ApiModelProperty(value = "角色名称", required = true, example = "测试角色")
    private String name;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "状态(0-正常,1-停用)", required = true, example = "0")
    private String status;

    @ApiModelProperty(value = "数据权限(1-所有数据 2-部门及子部门数据 3-本部门数据 4-本人数据 5-自定义部门数据)", example = "4")
    private Integer dataScope;

    @ApiModelProperty(value = "自定义权限部门ID集合（dataScope=5时有效）")
    private List<Long> deptIds;

    @ApiModelProperty(value = "备注", example = "这是角色备注")
    private String remark;

}
