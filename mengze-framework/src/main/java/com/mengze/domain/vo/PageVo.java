package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "分页响应对象")
public class PageVo
{
    @ApiModelProperty(value = "数据列表")
    private List rows;
    
    @ApiModelProperty(value = "总记录数")
    private Long total;
}
