package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Article;
import com.mengze.domain.vo.ArticleListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.SearchArticleListVo;
import com.mengze.mapper.ArticleMapper;
import com.mengze.service.CategoryService;
import com.mengze.service.SearchService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 搜索接口服务实现类
 * 
 * @author mengze
 * @since 2026-03-08
 */
@Service("searchService")
public class SearchServiceImpl implements SearchService
{
    @Autowired
    private ArticleMapper articleMapper;
    
    @Autowired
    private CategoryService categoryService;

    /**
     * 搜索文章
     * 
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @param keyword 搜索关键词
     * @return 搜索结果
     */
    @Override
    public ResponseResult search(Integer pageNum, Integer pageSize, String keyword) {
        // 参数校验
        if (StringUtils.isBlank(keyword)) {
            return ResponseResult.okResult(new PageVo());
        }

        final String searchKeyword = keyword.trim();
        
        // 构建查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        
        // 只搜索已发布状态的文章
        queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);
        
        // 未删除的文章
        queryWrapper.eq(Article::getDelFlag, SystemConstants.NOT_DELETED);
        
        // 在标题和内容中进行模糊匹配
        queryWrapper.and(wrapper -> 
            wrapper.like(Article::getTitle, searchKeyword)
                   .or()
                   .like(Article::getContent, searchKeyword)
        );
        
        // 排序：置顶文章优先，然后按创建时间降序
        queryWrapper.orderByDesc(Article::getIsTop);
        queryWrapper.orderByDesc(Article::getCreateTime);
        
        // 分页查询
        Page<Article> page = new Page<>(pageNum, pageSize);
        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
        
        // 获取查询结果
        List<Article> articles = articlePage.getRecords();
        
        // 填充分类名称
        articles.forEach(article -> {
            if (article.getCategoryId() != null) {
                try {
                    article.setCategoryName(categoryService.getById(article.getCategoryId()).getName());
                } catch (Exception e) {
                    // 如果分类不存在，设置为空字符串
                    article.setCategoryName("");
                }
            }
        });
        
        // 转换为VO对象（使用已经设置了categoryName的articles列表）
        List<SearchArticleListVo> searchArticleListVos = BeanCopyUtils.copyBeanList(articles, SearchArticleListVo.class);
        
        // 封装分页结果
        PageVo pageVo = new PageVo(searchArticleListVos, articlePage.getTotal());
        
        return ResponseResult.okResult(pageVo);
    }
}
