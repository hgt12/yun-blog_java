package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 添加分类请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "添加分类请求对象")
public class AddCategoryDto {

    @NotBlank(message = "分类名称不能为空")
    @ApiModelProperty(value = "分类名称", required = true, example = "技术分享")
    private String name;
    
    @ApiModelProperty(value = "分类描述", example = "技术相关的文章")
    private String description;

    @ApiModelProperty(value = "排序值", example = "0")
    private Integer sort;
    
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "状态（0-正常，1-禁用）", required = true, example = "0")
    private String status;
}
