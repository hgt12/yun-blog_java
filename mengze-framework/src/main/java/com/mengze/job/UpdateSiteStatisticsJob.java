package com.mengze.job;

import com.mengze.constants.RedisHashFieldConstants;
import com.mengze.constants.RedisKeyConstants;
import com.mengze.domain.entity.SiteStatistics;
import com.mengze.service.SiteStatisticsService;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

//更新站点 PV UV 定时任务
@Component
public class UpdateSiteStatisticsJob
{
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SiteStatisticsService siteStatisticsService;

    // 每10分钟同步网站 PV、UV 到数据库
    //@Scheduled(cron = "0/5 * * * * ?") //每五秒钟同步一次
    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateSiteStatistics()
    {
        SiteStatistics statistics = new SiteStatistics();
        statistics.setId(1L);

        Integer cachedViews = redisCache.getCacheMapValue(RedisKeyConstants.SITE_TOTAL_VIEWS_KEY, RedisHashFieldConstants.SITE_TOTAL_VIEWS_FIELD);
        if (cachedViews != null) {
            statistics.setTotalViews(cachedViews.longValue());
        }

        Integer cachedVisitors = redisCache.getCacheMapValue(RedisKeyConstants.SITE_TOTAL_VISITORS_KEY, RedisHashFieldConstants.SITE_TOTAL_VISITORS_FIELD);
        if (cachedVisitors != null) {
            statistics.setTotalVisitors(cachedVisitors.longValue());
        }

        siteStatisticsService.updateById(statistics);
        System.out.println("已同步站点浏览量");
    }
}
