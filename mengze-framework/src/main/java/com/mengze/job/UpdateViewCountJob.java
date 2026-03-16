package com.mengze.job;

import com.mengze.constants.SystemConstants;
import com.mengze.domain.entity.Article;
import com.mengze.domain.entity.SiteStatistics;
import com.mengze.domain.entity.VisitLog;
import com.mengze.service.ArticleService;
import com.mengze.service.SiteStatisticsService;
import com.mengze.service.VisitLogService;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//更新文章浏览量定时任务
@Component
public class UpdateViewCountJob {

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private SiteStatisticsService siteStatisticsService;

    @Autowired
    private VisitLogService visitLogService;

    // 每10分钟同步文章浏览量到数据库
    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateViewCount() {
        Map<String, Integer> viewCountMap = redisCache.getCacheMap(SystemConstants.ARTICLE_VIEW_COUNT);
        List<Article> articles = viewCountMap.entrySet()
                .stream()
                .map(entry -> new Article(Long.valueOf(entry.getKey()), entry.getValue().longValue()))
                .collect(Collectors.toList());
        articleService.updateBatchById(articles);
    }

    // 每10分钟同步网站 PV、UV 到数据库
    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateSiteStatistics()
    {
        SiteStatistics statistics = new SiteStatistics();
        statistics.setId(1L);

        Integer cachedViews = redisCache.getCacheMapValue(SystemConstants.SITE_TOTAL_VIEWS, SystemConstants.SITE_TOTAL_VIEWS_FIELD);
        if (cachedViews != null) {
            statistics.setTotalViews(cachedViews.longValue());
        }

        Integer cachedVisitors = redisCache.getCacheMapValue(SystemConstants.SITE_TOTAL_VISITORS, SystemConstants.SITE_TOTAL_VISITORS_FIELD);
        if (cachedVisitors != null) {
            statistics.setTotalVisitors(cachedVisitors.longValue());
        }

        siteStatisticsService.updateById(statistics);
    }

    // 每5分钟将 visit_log 队列批量 flush 到数据库
    @Scheduled(cron = "0 0/5 * * * ?")
    public void flushVisitLogs()
    {
        List<VisitLog> logs = redisCache.popAllFromList(SystemConstants.VISIT_LOG_QUEUE);
        if (logs != null && !logs.isEmpty()) {
            visitLogService.saveBatch(logs);
        }
    }
}
