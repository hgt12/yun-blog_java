package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 更新字典项请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "更新字典项请求对象")
public class UpdateDictItemDto {

    @NotBlank(message = "字典项标签不能为空")
    @ApiModelProperty(value = "字典项标签", required = true, example = "男")
    private String label;

    @NotBlank(message = "字典项值不能为空")
    @ApiModelProperty(value = " 字典项值", required = true, example = "1")
    private String value;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "标签类型，用于前端样式展示：`success`/`warning`/`info`/`primary`/`danger`/空", example = "success")
    private String tagType;
    
    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "状态（0-正常，1-禁用）", required = true, example = "0")
    private String status;
}
