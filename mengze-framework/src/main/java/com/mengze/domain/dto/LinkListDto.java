package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 友链查询请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "友链查询请求对象")
public class LinkListDto {

    @ApiModelProperty(value = "状态", example = "0")
    private String status;

    @ApiModelProperty(value = "关键字（用于模糊搜索友链名称）", example = "Fluid")
    private String keywords;
}
