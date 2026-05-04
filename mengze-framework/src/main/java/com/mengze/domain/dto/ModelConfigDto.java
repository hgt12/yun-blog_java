package com.mengze.domain.dto;

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
public class ModelConfigDto {
    /**
     * 模型名称
     */
    private String name;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 权重
     */
    private Float weight;
}
