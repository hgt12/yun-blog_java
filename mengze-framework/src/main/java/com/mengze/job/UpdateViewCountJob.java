package com.mengze.job;

import com.mengze.constants.RedisKeyConstants;
import com.mengze.domain.entity.Article;
import com.mengze.service.ArticleService;
import com.mengze.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//更新文章浏览量定时任务
@Slf4j
@Component
public class UpdateViewCountJob
{
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ArticleService articleService;

    // 每10分钟把 Redis 里的文章浏览量增量累加到数据库
    //@Scheduled(cron = "0/5 * * * * ?") //每五秒钟同步一次
    @Scheduled(cron = "0 0/10 * * * ?")
    public void updateViewCount()
    {
        //从Redis获取增量数据
        Map<String, Integer> viewCountDeltaMap = redisCache.getCacheMap(RedisKeyConstants.ARTICLE_VIEW_COUNT_INCREMENT_KEY);
        if (viewCountDeltaMap == null || viewCountDeltaMap.isEmpty()) {
            return;
        }

        List<Article> articlesToUpdate = new ArrayList<>();
        List<String> articleIds = new ArrayList<>();

        // 遍历每个增量条目并批量组装更新对象
        for (Map.Entry<String, Integer> entry : viewCountDeltaMap.entrySet())
        {
            Long articleId;
            try {
                articleId = Long.valueOf(entry.getKey());
            } catch (NumberFormatException e) {
                continue;
            }

            Integer delta = entry.getValue();
            if (delta == null || delta == 0) {
                continue;
            }

            Article article = articleService.getById(articleId);
            if (article == null) {
                continue;
            }

            long currentViewCount = article.getViewCount() == null ? 0L : article.getViewCount();
            article.setViewCount(currentViewCount + delta.longValue());
            articlesToUpdate.add(article);
            articleIds.add(entry.getKey());
        }

        if (articlesToUpdate.isEmpty()) {
            return;
        }

        //批量更新预览量
        articleService.updateBatchById(articlesToUpdate);
        for (String articleId : articleIds) {
            redisCache.delCacheMapValue(RedisKeyConstants.ARTICLE_VIEW_COUNT_INCREMENT_KEY, articleId);
        }
        log.info("已同步文章浏览量增量至数据库,更新数为：{}", articlesToUpdate.size());
    }
}
