package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.RecordVisitDto;
import com.mengze.domain.vo.SiteInfoVo;
import com.mengze.service.SiteStatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 网站统计相关接口
 */
@RestController
@RequestMapping("/statistics")
@Api(tags = "网站统计", description = "网站统计相关接口")
public class SiteStatisticsController {

    @Autowired
    private SiteStatisticsService siteStatisticsService;

    //由于前后台系统使用的是同一个网站访问数据统计接口和方法
    //而这里的网站统计信息又是从 网站访问数据统计接口 中存入 redis 的数据来返回的
    //所以这个接口返回的数据实际上是 前台 + 后台 两个系统的访问数据的总和
    //在后台系统中看到的数据也同理
    @GetMapping("/siteInfo")
    @SystemLog(businessName = "获取网站统计信息")
    @ApiOperation(value = "获取网站统计信息", notes = "获取总访问量、总访客数", response = SiteInfoVo.class)
    public ResponseResult getSiteInfo()
    {
        return siteStatisticsService.getSiteInfo();
    }

    @PostMapping("/recordVisit")
    @ApiOperation(value = "记录访问日志", notes = "记录用户访问，统计PV和UV", response = String.class)
    public ResponseResult recordVisit(@Valid @RequestBody RecordVisitDto recordVisitDto, HttpServletRequest request)
    {
        return siteStatisticsService.recordVisit(recordVisitDto, request);
    }
}
