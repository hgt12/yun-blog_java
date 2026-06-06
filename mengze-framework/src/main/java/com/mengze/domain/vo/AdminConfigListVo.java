package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "系统配置响应对象")
public class AdminConfigListVo
{
    @ApiModelProperty(value = "配置ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "配置名称", example = "网站标题")
    private String configName;

    @ApiModelProperty(value = "配置键", example = "site.title")
    private String configKey;

    @ApiModelProperty(value = "配置值", example = "梦泽博客")
    private String configValue;

    @ApiModelProperty(value = "描述", example = "网站的标题配置")
    private String remark;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

}
