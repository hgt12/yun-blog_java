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

    /**
     * 获取网站统计信息（总访问量、总访客数、文章数、分类数、标签数）
     */
    @GetMapping("/siteInfo")
    @SystemLog(businessName = "获取网站统计信息")
    @ApiOperation(value = "获取网站统计信息", notes = "获取总访问量、总访客数、文章数、分类数、标签数", response = SiteInfoVo.class)
    public ResponseResult getSiteInfo() {
        return siteStatisticsService.getSiteInfo();
    }

    /**
     * 记录访问日志（统计PV和UV）
     */
    @PostMapping("/recordVisit")
    @ApiOperation(value = "记录访问日志", notes = "记录用户访问，统计PV和UV")
    public ResponseResult recordVisit(@Valid @RequestBody RecordVisitDto recordVisitDto, HttpServletRequest request) {
        return siteStatisticsService.recordVisit(recordVisitDto, request);
    }
}
