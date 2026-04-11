package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.TagListDto;
import com.mengze.domain.entity.Article;
import com.mengze.domain.entity.ArticleTag;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.TagVo;
import com.mengze.mapper.ArticleTagMapper;
import com.mengze.mapper.TagMapper;
import com.mengze.domain.entity.Tag;
import com.mengze.service.ArticleService;
import com.mengze.service.TagService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 标签表(Tag)表服务实现类
 *
 * @author makejava
 * @since 2026-03-22 10:50:19
 */
@Service("tagService")
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService
{

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    //前台获取标签列表
    @Override
    public ResponseResult getTagList()
    {
        //先去查询文章表状态为已发布的
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);
        List<Article> articleList = articleService.list(articleWrapper);

        //获取已发布文章的ID集合
        Set<Long> articleIds = articleList.stream()
                .map(Article::getId)
                .collect(Collectors.toSet());

        //查询这些文章关联的标签
        LambdaQueryWrapper<ArticleTag> articleTagWrapper = new LambdaQueryWrapper<>();
        if (!articleIds.isEmpty()) {
            articleTagWrapper.in(ArticleTag::getArticleId, articleIds);
        } else {
            //没有已发布文章时返回空结果
            articleTagWrapper.eq(ArticleTag::getArticleId, -1L);
        }
        List<ArticleTag> articleTagList = articleTagMapper.selectList(articleTagWrapper);

        //获取标签ID集合并去重
        Set<Long> tagIds = articleTagList.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toSet());

        //统计每个标签关联的文章数量
        Map<Long, Long> tagArticleCountMap = articleTagList.stream()
                .collect(Collectors.groupingBy(ArticleTag::getTagId, Collectors.counting()));

        //查询标签详细信息
        LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
        if (!tagIds.isEmpty()) {
            tagWrapper.in(Tag::getId, tagIds);
        } else {
            //没有标签时返回空结果
            tagWrapper.eq(Tag::getId, -1L);
        }
        tagWrapper.eq(Tag::getDelFlag, SystemConstants.NOT_DELETED);
        List<Tag> tagList = list(tagWrapper);

        //转换为TagVo并设置文章数量
        List<TagVo> tagVoList = tagList.stream()
                .map(tag -> {
                    TagVo tagVo = BeanCopyUtils.copyBean(tag, TagVo.class);
                    tagVo.setArticleCount(tagArticleCountMap.getOrDefault(tag.getId(), 0L).intValue());
                    return tagVo;
                })
                .collect(Collectors.toList());

        return ResponseResult.okResult(tagVoList);
    }

}

