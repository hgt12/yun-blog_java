package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 标签操作请求DTO
 */
@Data
@ApiModel(description = "标签操作请求对象")
public class TagListDto {
    
    @ApiModelProperty(value = "标签ID（更新时必填）", example = "1")
    private Long id;

    @ApiModelProperty(value = "标签ID列表", required = true, example = "[1,2,3]")
    private List<Long> ids;

    @ApiModelProperty(value = "关键字（用于模糊搜索标签名称）", example = "Vue")
    private String keywords;
}
