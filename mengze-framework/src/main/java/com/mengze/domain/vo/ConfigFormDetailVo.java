package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "后台配置表单详情响应对象")
public class ConfigFormDetailVo
{
    @ApiModelProperty(value = "配置ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "配置名称", example = "这里是配置名称")
    private String configName;

    @ApiModelProperty(value = "配置键", example = "这里是配置键")
    private String configKey;

    @ApiModelProperty(value = "配置值", example = "这里是配置值")
    private String configValue;

    @ApiModelProperty(value = "备注", example = "对该配的一些描述")
    private String remark;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "配置分类（0-前台配置，1-后台配置）", example = "0")
    private String configType;
}
