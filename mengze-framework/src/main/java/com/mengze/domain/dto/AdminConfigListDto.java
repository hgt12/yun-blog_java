package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置查询请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "配置查询请求对象")
public class AdminConfigListDto {

    @ApiModelProperty(value = "关键字（用于模糊搜索配置）", example = "配置")
    private String keywords;
}
