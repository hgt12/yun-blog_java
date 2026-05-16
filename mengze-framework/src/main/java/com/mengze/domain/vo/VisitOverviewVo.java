package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "访问概览响应对象")
public class VisitOverviewVo
{
    @ApiModelProperty(value = "今天访客UV数", example = "92")
    private Long todayUvCount;

    @ApiModelProperty(value = "总访客UV数", example = "12030")
    private Long totalUvCount;

    @ApiModelProperty(value = "访客UV增长率", example = "12.5")
    private Double uvGrowthRate;

    @ApiModelProperty(value = "今天浏览量PV数", example = "150")
    private Long todayPvCount;

    @ApiModelProperty(value = "总浏览量PV数", example = "34210")
    private Long totalPvCount;

    @ApiModelProperty(value = "浏览量PV增长率", example = "8.3")
    private Double pvGrowthRate;
}