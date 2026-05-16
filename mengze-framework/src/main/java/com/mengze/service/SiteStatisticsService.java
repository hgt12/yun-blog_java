package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.RecordVisitDto;
import com.mengze.domain.entity.SiteStatistics;

import javax.servlet.http.HttpServletRequest;

/**
 * 网站统计表(SiteStatistics)表服务接口
 */
public interface SiteStatisticsService extends IService<SiteStatistics> {
    
    ResponseResult getSiteInfo();
    
    ResponseResult recordVisit(RecordVisitDto recordVisitDto, HttpServletRequest request);
}
