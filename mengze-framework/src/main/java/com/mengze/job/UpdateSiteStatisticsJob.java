package com.mengze.job;

import com.mengze.constants.RedisHashFieldConstants;
import com.mengze.constants.RedisKeyConstants;
import com.mengze.domain.entity.SiteStatistics;
import com.mengze.service.SiteStatisticsService;
import com.mengze.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//更新站点 PV UV 定时任务
@Slf4j
@Component
public class UpdateSiteStatisticsJob
{
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SiteStatisticsService siteStatisticsService;

    // 每10分钟把 Redis 增量累加到数据库
    //@Scheduled(cron = "0/5 * * * * ?") //每五秒钟同步一次
    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateSiteStatistics()
    {
        // 从redis中获取 pv uv
        Integer pvDelta = redisCache.getCacheMapValue(RedisKeyConstants.SITE_VIEWS_ADD_INCREMENT_KEY, RedisHashFieldConstants.SITE_TOTAL_VIEWS_FIELD);
        Integer uvDelta = redisCache.getCacheMapValue(RedisKeyConstants.SITE_VISITORS_INCREMENT_KEY, RedisHashFieldConstants.SITE_TOTAL_VISITORS_FIELD);

        if ((pvDelta == null || pvDelta == 0) && (uvDelta == null || uvDelta == 0)) {
            return;
        }

        SiteStatistics statistics = siteStatisticsService.getById(1L);
        if (statistics == null) {
            statistics = new SiteStatistics();
            statistics.setId(1L);
            statistics.setTotalViews(0L);
            statistics.setTotalVisitors(0L);
        }

        //更新站点PV UV
        long currentViews = statistics.getTotalViews() == null ? 0L : statistics.getTotalViews();
        long currentVisitors = statistics.getTotalVisitors() == null ? 0L : statistics.getTotalVisitors();
        statistics.setTotalViews(currentViews + (pvDelta == null ? 0L : pvDelta.longValue()));
        statistics.setTotalVisitors(currentVisitors + (uvDelta == null ? 0L : uvDelta.longValue()));
        siteStatisticsService.updateById(statistics);

        if (pvDelta != null && pvDelta != 0) {
            redisCache.delCacheMapValue(RedisKeyConstants.SITE_VIEWS_ADD_INCREMENT_KEY, RedisHashFieldConstants.SITE_TOTAL_VIEWS_FIELD);
        }
        if (uvDelta != null && uvDelta != 0) {
            redisCache.delCacheMapValue(RedisKeyConstants.SITE_VISITORS_INCREMENT_KEY, RedisHashFieldConstants.SITE_TOTAL_VISITORS_FIELD);
        }

        log.info("已同步站点浏览量增量至数据库(PV:{} 和 UV:{})! ", statistics.getTotalViews(), statistics.getTotalVisitors());
    }
}
