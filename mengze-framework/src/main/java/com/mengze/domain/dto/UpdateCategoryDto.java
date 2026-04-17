package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新分类请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "更新分类请求对象")
public class UpdateCategoryDto {
    
    @ApiModelProperty(value = "分类ID", required = true, example = "1")
    private Long id;
    
    @ApiModelProperty(value = "分类名称", required = true, example = "技术分享")
    private String name;
    
    @ApiModelProperty(value = "分类描述", example = "技术相关的文章")
    private String description;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;
}
