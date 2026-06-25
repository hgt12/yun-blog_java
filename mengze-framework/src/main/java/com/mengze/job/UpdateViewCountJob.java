package com.mengze.job;

import com.mengze.constants.RedisHashFieldConstants;
import com.mengze.constants.RedisKeyConstants;
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
public class UpdateViewCountJob
{
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ArticleService articleService;

    // 每10分钟同步文章浏览量到数据库
    //@Scheduled(cron = "0/5 * * * * ?") //每五秒钟同步一次
    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateViewCount() {
        Map<String, Integer> viewCountMap = redisCache.getCacheMap(RedisKeyConstants.ARTICLE_VIEW_COUNT_KEY);
        List<Article> articles = viewCountMap.entrySet()
                .stream()
                .map(entry -> new Article(Long.valueOf(entry.getKey()), entry.getValue().longValue()))
                .collect(Collectors.toList());
        articleService.updateBatchById(articles);
        System.out.println("已同步文章浏览量");
    }
}
