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
@ApiModel(description = "访问趋势响应对象")
public class VisitTrendVo
{
    @ApiModelProperty(value = "时间范围", example = "[\"2026-03-24\", \"2026-03-25\"]")
    private List<String> dates;

    @ApiModelProperty(value = "浏览量PV", example = "[120, 150]")
    private List<Integer> pvList;

    @ApiModelProperty(value = "访客数UV", example = "[80, 92]")
    private List<Integer> uvList;

    @ApiModelProperty(value = "ip数", example = "[70, 85]")
    private List<Integer> ipList;
}