package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.CategoryListDto;
import com.mengze.domain.entity.Article;
import com.mengze.domain.entity.Category;
import com.mengze.domain.entity.Tag;
import com.mengze.domain.vo.*;
import com.mengze.service.AdminCategoryService;
import com.mengze.service.ArticleService;
import com.mengze.service.CategoryService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 分类管理服务实现类
 *
 * @author mengze
 */
@Service
public class AdminCategoryServiceImpl implements AdminCategoryService
{
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ArticleService articleService;

    //获取分类列表
    @Override
    public ResponseResult categoryList(Integer pageNum, Integer pageSize, CategoryListDto categoryListDto)
    {
        // 构建分类查询条件
        LambdaQueryWrapper<Category> categoryQueryWrapper = new LambdaQueryWrapper<>();

        //根据分类名称模糊查询
        if (StringUtils.hasText(categoryListDto.getKeywords())){
            categoryQueryWrapper.like(Category::getName, categoryListDto.getKeywords());
        }

        //只查询未删除的分类
        categoryQueryWrapper.eq(Category::getDelFlag, SystemConstants.NOT_DELETED);

        //根据状态筛选
        if (StringUtils.hasText(categoryListDto.getStatus())) {
            categoryQueryWrapper.eq(Category::getStatus, categoryListDto.getStatus());
        }

        //以创建时间降序
        categoryQueryWrapper.orderByDesc(Category::getCreateTime);

        //分页查询
        Page<Category> page = new Page<>(pageNum, pageSize);
        categoryService.page(page, categoryQueryWrapper);

        //查询分类表
        List<Category> categoryList = page.getRecords();

        //封装查询结果为Vo返回
        final List<AdminCategoryListVo> categoryListVos = BeanCopyUtils.copyBeanList(categoryList, AdminCategoryListVo.class);

        // 构建文章查询条件（只查询未删除且已发布的文章）
        LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
        articleQueryWrapper.eq(Article::getDelFlag, SystemConstants.NOT_DELETED)
                          .eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);
        List<Article> articleList = articleService.list(articleQueryWrapper);

        //统计每个分类下的文章数量
        for (AdminCategoryListVo categoryListVo : categoryListVos) {
            long count = articleList.stream()
                    .filter(article -> article.getCategoryId().equals(categoryListVo.getId()))
                    .count();
            categoryListVo.setArticleCount((int) count);
        }

        PageVo pageVo = new PageVo(categoryListVos, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }

    //获取分类下拉选择列表
    @Override
    public ResponseResult getCategoryOptions()
    {
        //根据分类id查询出所有分类选项
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Category::getStatus, SystemConstants.STATUS_NORMAL)
                    .orderByDesc(Category::getId);
        //封装成一个集合
        List<Category> categoryList = categoryService.list(queryWrapper);

        //转换成Vo
        List<CategoryOptionVo> categoryOptionVoList = BeanCopyUtils.copyBeanList(categoryList, CategoryOptionVo.class);

        return ResponseResult.okResult(categoryOptionVoList);
    }
}
