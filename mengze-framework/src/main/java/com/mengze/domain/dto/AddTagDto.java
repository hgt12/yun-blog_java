package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加标签请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "添加标签请求对象")
public class AddTagDto {
    
    @ApiModelProperty(value = "标签名称", required = true, example = "Java")
    private String name;
    
    @ApiModelProperty(value = "备注", example = "Java相关标签")
    private String remark;

    @ApiModelProperty(value = "排序值", example = "0")
    private Integer sort;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", required = true, example = "0")
    private String status;
}
