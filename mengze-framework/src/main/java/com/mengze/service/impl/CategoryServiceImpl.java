package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Article;
import com.mengze.domain.vo.CategoryVo;
import com.mengze.mapper.CategoryMapper;
import com.mengze.domain.entity.Category;
import com.mengze.service.ArticleService;
import com.mengze.service.CategoryService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 分类表(Category)表服务实现类
 *
 * @author makejava
 * @since 2025-11-26 16:28:19
 */
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private ArticleService articleService;

    @Override
    public ResponseResult getCategoryList()
    {
        //先去查询文章表状态为已发布的
        LambdaQueryWrapper<Article> articleWrapper = new LambdaQueryWrapper<>();
        articleWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);//查询文章状态
        List<Article> articleList =  articleService.list(articleWrapper);
        //获取文章的分类id，并且去重
        Set<Long> categoryId = articleList.stream()
                .map(Article::getCategoryId)
                .collect(Collectors.toSet());
        //查询分类表
        List<Category> categories = listByIds(categoryId);
        //保证是正常状态的分类
        categories =  categories.stream()
                .filter(category -> SystemConstants.STATUS_NORMAL.equals(category.getStatus()))
                .collect(Collectors.toList());
        // 封装成Vo
        final List<CategoryVo> categoryVos = BeanCopyUtils.copyBeanList(categories, CategoryVo.class);
        
        //统计每个分类下的文章数量
        for (CategoryVo categoryVo : categoryVos) {
            long count = articleList.stream()
                    .filter(article -> article.getCategoryId().equals(categoryVo.getId()))
                    .count();
            categoryVo.setArticleCount((int) count);
        }
        
        return ResponseResult.okResult(categoryVos);
    }
}

