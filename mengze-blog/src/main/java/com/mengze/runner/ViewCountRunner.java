package com.mengze.runner;

import com.mengze.constants.RedisHashFieldConstants;
import com.mengze.constants.RedisKeyConstants;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.entity.Article;
import com.mengze.domain.entity.SiteStatistics;
import com.mengze.mapper.ArticleMapper;
import com.mengze.mapper.SiteStatisticsMapper;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

//应用启动时初始化缓存
@Component
public class ViewCountRunner implements CommandLineRunner
{
    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private SiteStatisticsMapper siteStatisticsMapper;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void run(String... args) throws Exception
    {
        //查询博客信息（文章id和ViewCount）
        List<Article> articles = articleMapper.selectList(null);
        Map<String, Integer> viewCountMap = articles.stream()
                .collect(Collectors.toMap(article -> article.getId().toString(), new Function<Article, Integer>() {
                    @Override
                    public Integer apply(Article article) {
                        return article.getViewCount().intValue();
                    }
                }));

        //存储文章预览量到redis
        redisCache.setCacheMap(RedisKeyConstants.ARTICLE_VIEW_COUNT_KEY, viewCountMap);

        //加载网站 PV 和 UV 到 Redis
        SiteStatistics statistics = siteStatisticsMapper.selectById(1L);
        long totalViews = (statistics != null && statistics.getTotalViews() != null) ? statistics.getTotalViews() : 0L;
        long totalVisitors = (statistics != null && statistics.getTotalVisitors() != null) ? statistics.getTotalVisitors() : 0L;

        redisCache.setCacheMapValue(RedisKeyConstants.SITE_TOTAL_VIEWS_KEY, RedisHashFieldConstants.SITE_TOTAL_VIEWS_FIELD, (int) totalViews);
        redisCache.setCacheMapValue(RedisKeyConstants.SITE_TOTAL_VISITORS_KEY, RedisHashFieldConstants.SITE_TOTAL_VISITORS_FIELD, (int) totalVisitors);

        System.out.println("! ======程序初始化完成====== !");
    }
}
