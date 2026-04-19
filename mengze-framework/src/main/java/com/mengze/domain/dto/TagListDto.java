package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 标签操作请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "标签操作请求对象")
public class TagListDto {

    @ApiModelProperty(value = "标签ID列表", required = true, example = "[1,2,3]")
    private List<Long> ids;

    @ApiModelProperty(value = "状态", example = "0")
    private String status;

    @ApiModelProperty(value = "关键字（用于模糊搜索标签名称）", example = "Vue")
    private String keywords;
}
