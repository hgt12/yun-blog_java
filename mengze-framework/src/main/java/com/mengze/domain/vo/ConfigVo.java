package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 前后台获取配置VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "前后台公共配置响应对象")
public class ConfigVo {

    @ApiModelProperty(value = "配置键", example = "site.name")
    private String configKey;

    @ApiModelProperty(value = "配置值", example = "云梦泽")
    private String configValue;
}
