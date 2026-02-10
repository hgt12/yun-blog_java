package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Article;
import com.mengze.domain.vo.ArchiveArticleVo;
import com.mengze.domain.vo.ArchiveVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.mapper.ArticleMapper;
import com.mengze.service.ArchiveService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArchiveServiceImpl implements ArchiveService
{

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public ResponseResult getArchiveList()
    {
        // 查询所有已发布的文章，按创建时间倒序
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL)
                .eq(Article::getDelFlag, SystemConstants.NOT_DELETED)
                .orderByDesc(Article::getCreateTime);

        List<Article> articles = articleMapper.selectList(queryWrapper);

        // 按年月分组
        Map<String, List<Article>> groupedArticles = articles.stream()
                .collect(Collectors.groupingBy(article -> {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(article.getCreateTime());
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH) + 1;
                    return year + "-" + month;
                }, LinkedHashMap::new, Collectors.toList()));

        // 转换为 ArchiveVo
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<ArchiveVo> archiveVos = groupedArticles.entrySet().stream()
                .map(entry -> {
                    String[] yearMonth = entry.getKey().split("-");
                    Integer year = Integer.parseInt(yearMonth[0]);
                    Integer month = Integer.parseInt(yearMonth[1]);

                    List<ArchiveArticleVo> articleVos = entry.getValue().stream()
                            .map(article -> new ArchiveArticleVo(
                                    article.getId(),
                                    article.getTitle(),
                                    sdf.format(article.getCreateTime())
                            ))
                            .collect(Collectors.toList());

                    return new ArchiveVo(year, month, articleVos.size(), articleVos);
                })
                .collect(Collectors.toList());

        return ResponseResult.okResult(archiveVos);
    }

    @Override
    public ResponseResult getArticlesByYearMonth(Integer year, Integer month, Integer pageNum, Integer pageSize)
    {
        // 构建查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL)
                .eq(Article::getDelFlag, SystemConstants.NOT_DELETED)
                .apply("YEAR(create_time) = {0}", year)
                .apply("MONTH(create_time) = {0}", month)
                .orderByDesc(Article::getCreateTime);

        // 分页查询
        Page<Article> page = new Page<>(pageNum, pageSize);
        articleMapper.selectPage(page, queryWrapper);

        // 转换为 VO
        List<ArchiveArticleVo> articleVos = page.getRecords().stream()
                .map(article -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    return new ArchiveArticleVo(
                            article.getId(),
                            article.getTitle(),
                            sdf.format(article.getCreateTime())
                    );
                })
                .collect(Collectors.toList());

        PageVo pageVo = new PageVo(articleVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }
}
