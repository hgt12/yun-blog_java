package com.mengze.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置缓存对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfigCacheVo
{
    @ApiModelProperty(value = "配置键", example = "site.name")
    private String configKey;

    @ApiModelProperty(value = "配置值", example = "云梦泽的个人博客")
    private String configValue;

    @ApiModelProperty(value = "配置分类（0-前台配置，1-后台配置）", example = "0")
    private String configType;
}
