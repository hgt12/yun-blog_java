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

//归档
@Service
public class ArchiveServiceImpl implements ArchiveService
{

    @Autowired
    private ArticleMapper articleMapper;

    @Override
    public ResponseResult getArchiveList(Integer pageNum, Integer pageSize)
    {
        // 构建查询条件：查询所有已发布的文章，按创建时间倒序，查询分类状态正常的文章（使用子查询）
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL)
                .eq(Article::getDelFlag, SystemConstants.NOT_DELETED)
                .orderByDesc(Article::getCreateTime)
                .inSql(Article::getCategoryId,
                        "SELECT id FROM category WHERE status = '0' AND del_flag = '0'");

        // 分页查询
        Page<Article> page = new Page<>(pageNum, pageSize);
        articleMapper.selectPage(page, queryWrapper);

        // 转换为 VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<ArchiveArticleVo> articleVos = page.getRecords().stream()
                .map(article -> new ArchiveArticleVo(
                        article.getId(),
                        article.getTitle(),
                        sdf.format(article.getCreateTime())
                ))
                .collect(Collectors.toList());

        PageVo pageVo = new PageVo(articleVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }
}
