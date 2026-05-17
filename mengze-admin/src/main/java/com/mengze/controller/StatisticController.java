package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.RecordVisitDto;
import com.mengze.service.SiteStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

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

    //由于前后台系统使用的是同一个网站访问数据统计接口和方法
    //而这里的网站统计信息又是从 网站访问数据统计接口 中存入 redis 的数据来返回的
    //所以这个接口返回的数据实际上是 前台 + 后台 两个系统的访问数据的总和
    //在前台系统中页脚里看到的数据也同理
    @GetMapping("/visits/overview")
    @ApiOperation(value = "获取访问概览", notes = "返回今日与总访问统计概览")
    public ResponseResult getVisitOverview() {
        return siteStatisticsService.getVisitOverview();
    }

    @PostMapping("/visits/recordVisit")
    @ApiOperation(value = "后台记录访问日志", notes = "记录后台用户访问", response = String.class)
    public ResponseResult recordVisit(@Valid @RequestBody RecordVisitDto recordVisitDto, HttpServletRequest request)
    {
        return siteStatisticsService.recordVisit(recordVisitDto, request);
    }
}
