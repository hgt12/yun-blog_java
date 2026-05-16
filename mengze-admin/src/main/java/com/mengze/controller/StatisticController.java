package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.service.SiteStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/statistics")
@Api(tags = "浏览量统计", description = "统计接口")
public class StatisticController
{
    @Autowired
    private SiteStatisticsService siteStatisticsService;

    @GetMapping("/visits/trend")
    @ApiOperation(value = "获取访问趋势", notes = "返回指定日期范围内的 PV、UV、IP 趋势")
    public ResponseResult getVisitTrend(
            @RequestParam String startDate,
            @RequestParam String endDate
    ) {
        return siteStatisticsService.getVisitTrend(startDate, endDate);
    }

    @GetMapping("/visits/overview")
    @ApiOperation(value = "获取访问概览", notes = "返回今日与总访问统计概览")
    public ResponseResult getVisitOverview() {
        return siteStatisticsService.getVisitOverview();
    }
}
