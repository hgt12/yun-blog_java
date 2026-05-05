package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI模型配置
 * @author mengze
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "AI模型配置请求对象")
public class ModelConfigDto {
    
    @ApiModelProperty(value = "模型名称", example = "gpt-3.5-turbo")
    private String name;
    
    @ApiModelProperty(value = "优先级", example = "1")
    private Integer priority;
    
    @ApiModelProperty(value = "权重", example = "0.8")
    private Float weight;
}
