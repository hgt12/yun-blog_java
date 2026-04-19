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
}
