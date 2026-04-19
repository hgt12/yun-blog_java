package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分类列表请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "分类列表请求对象")
public class CategoryListDto {

    @ApiModelProperty(value = "状态", example = "0")
    private String status;

    @ApiModelProperty(value = "分类ID列表", required = true, example = "[1,2,3]")
    private List<Long> ids;

    @ApiModelProperty(value = "关键字（用于模糊搜索分类名称）", example = "Vue")
    private String keywords;
}
