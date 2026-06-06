package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

/**
 * 配置查询请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "配置查询请求对象")
public class ConfigListDto {

    @ApiModelProperty(value = "关键字（配置键或配置名称）", example = "配置")
    private String keywords;

    @Pattern(regexp = "^$|^[01]$", message = "配置分类只能为0或1")
    @ApiModelProperty(value = "配置分类（0-前台配置，1-后台配置）", example = "0")
    private String configType;
}
