package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 添加字典请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "添加字典请求对象")
public class AddDictDto {

    @NotBlank(message = "字典名称不能为空")
    @ApiModelProperty(value = "字典名称", required = true, example = "性别")
    private String name;

    @NotBlank(message = "字典名称不能为空")
    @ApiModelProperty(value = " 字典编码（唯一）", required = true, example = "sex")
    private String dictCode;

    @ApiModelProperty(value = "备注", example = "这是用户的性别")
    private String remark;
    
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "状态（0-正常，1-禁用）", required = true, example = "0")
    private String status;
}
