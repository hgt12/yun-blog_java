package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 更新标签请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "更新标签请求对象")
public class UpdateTagDto {
    
    @ApiModelProperty(value = "标签ID", required = true, example = "1")
    private Long id;
    
    @NotBlank(message = "标签名称不能为空")
    @ApiModelProperty(value = "标签名称", required = true, example = "Java")
    private String name;
    
    @ApiModelProperty(value = "备注", example = "Java相关标签")
    private String remark;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @Pattern(regexp = "^$|^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;
}
