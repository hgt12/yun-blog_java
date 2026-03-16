package com.mengze.runner;

import com.mengze.constants.SystemConstants;
import com.mengze.domain.entity.Article;
import com.mengze.mapper.ArticleMapper;
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

        //存储到redis
        redisCache.setCacheMap(SystemConstants.ARTICLE_VIEW_COUNT, viewCountMap);
        System.out.println("! ======程序初始化完成====== !");
    }
}
