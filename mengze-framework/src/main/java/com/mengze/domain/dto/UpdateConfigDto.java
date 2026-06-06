package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 更新配置请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "更新配置请求对象")
public class UpdateConfigDto {

    @NotBlank(message = "配置名称不能为空")
    @ApiModelProperty(value = "配置名称", required = true, example = "配置名称")
    private String configName;

    @NotBlank(message = "配置键不能为空")
    @ApiModelProperty(value = "配置键（唯一）", required = true, example = "site_name")
    private String configKey;

    @NotBlank(message = "配置键不能为空")
    @ApiModelProperty(value = "配置值", required = true, example = "云梦泽的个人博客")
    private String configValue;

    @ApiModelProperty(value = "备注", example = "给配置添加描述")
    private String remark;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @NotBlank(message = "配置分类不能为空")
    @Pattern(regexp = "^$|^[01]$", message = "配置分类只能为0或1")
    @ApiModelProperty(value = "配置分类（0-前台配置，1-后台配置）", example = "0")
    private String configType;
}
