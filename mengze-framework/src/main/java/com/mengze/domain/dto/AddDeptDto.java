package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 添加部门请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "添加部门请求对象")
public class AddDeptDto {

    @NotBlank(message = "部门名称不能为空")
    @ApiModelProperty(value = "部门名称", required = true, example = "技术部")
    private String name;
    
    @ApiModelProperty(value = "部门编码", example = "TECH")
    private String code;

    @NotNull(message = "上级部门不能为空")
    @ApiModelProperty(value = "父部门ID（顶级部门可传'0'）", required = true, example = "1")
    private Long parentId;

    @ApiModelProperty(value = "排序值", example = "0")
    private Integer sort;
    
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "状态（0-正常，1-禁用）", required = true, example = "0")
    private String status;
}
