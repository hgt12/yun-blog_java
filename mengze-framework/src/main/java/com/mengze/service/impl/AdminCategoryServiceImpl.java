package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddCategoryDto;
import com.mengze.domain.dto.CategoryListDto;
import com.mengze.domain.dto.UpdateCategoryDto;
import com.mengze.domain.entity.Article;
import com.mengze.domain.entity.Category;
import com.mengze.domain.vo.*;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.service.AdminCategoryService;
import com.mengze.service.ArticleService;
import com.mengze.service.CategoryService;
import com.mengze.utils.BeanCopyUtils;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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

        //根据状态筛选
        if (StringUtils.hasText(categoryListDto.getStatus())) {
            categoryQueryWrapper.eq(Category::getStatus, categoryListDto.getStatus());
        }

        //先以排序序号升序排序和序号相同时按创建时间降序排序
        categoryQueryWrapper.orderByAsc(Category::getSort)
                            .orderByDesc(Category::getCreateTime);

        //分页查询
        Page<Category> page = new Page<>(pageNum, pageSize);
        categoryService.page(page, categoryQueryWrapper);

        //查询分类表
        List<Category> categoryList = page.getRecords();

        //封装查询结果为Vo返回
        final List<AdminCategoryListVo> categoryListVos = BeanCopyUtils.copyBeanList(categoryList, AdminCategoryListVo.class);

        // 构建文章查询条件
        LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
        articleQueryWrapper.select(Article::getCategoryId);
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

        //查询状态正常的分类
        queryWrapper.eq(Category::getStatus, SystemConstants.STATUS_NORMAL)
                    .orderByDesc(Category::getId);
        //封装成一个集合
        List<Category> categoryList = categoryService.list(queryWrapper);

        //转换成Vo
        List<CategoryOptionVo> categoryOptionVoList = BeanCopyUtils.copyBeanList(categoryList, CategoryOptionVo.class);

        return ResponseResult.okResult(categoryOptionVoList);
    }

    // 获取分类表单数据
    @Override
    public ResponseResult<CategoryFormDetailVo> categoryFormDetail(Long id)
    {
        //根据分类id 查询分类
        Category category = categoryService.getById(id);

        //转换成Vo
        CategoryFormDetailVo categoryFormDetailVo = BeanCopyUtils.copyBean(category, CategoryFormDetailVo.class);

        //返回
        return ResponseResult.okResult(categoryFormDetailVo);
    }

    //新增分类
    @Override
    public ResponseResult addCategory(AddCategoryDto addCategoryDto)
    {
        //添加分类
        Category category = BeanCopyUtils.copyBean(addCategoryDto, Category.class);
        categoryService.save(category);

        return ResponseResult.okResult();
    }

    //编辑分类
    @Override
    public ResponseResult updateCategory(Long id, UpdateCategoryDto updateCategoryDto)
    {
        //获取分类id
        updateCategoryDto.setId(id);

        //获取旧的分类信息
        Category oldCategory = categoryService.getById(id);

        String msg = null;

        //检查是否将分类状态从正常改为禁用
        if (SystemConstants.STATUS_NORMAL.equals(oldCategory.getStatus())
            && !"0".equals(updateCategoryDto.getStatus())) {
            //统计该分类下的文章数量
            LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
            articleQueryWrapper.eq(Article::getCategoryId, id);
            long articleCount = articleService.count(articleQueryWrapper);

            msg = String.format("分类【%s】已被禁用，该分类下有 %d 篇文章将不会在 前台 中显示", oldCategory.getName(), articleCount);
            log.info("警告：{}", msg);
        }

        //更新分类信息
        Category category = BeanCopyUtils.copyBean(updateCategoryDto, Category.class);
        categoryService.updateById(category);

        if (msg != null) {
            return ResponseResult.okResult(msg);
        }

        return ResponseResult.okResult();
    }

    //批量删除分类（逻辑删除）
    @Override
    public ResponseResult deleteCategory(Long[] ids)
    {
        //获取分类id列表
        List<Long> idList = List.of(ids);

        //收集所有有文章的分类信息
        StringBuilder errorMsg = new StringBuilder();

        //检查每个分类下是否有文章
        for (Long categoryId : idList) {
            LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
            articleQueryWrapper.eq(Article::getCategoryId, categoryId);

            long articleCount = articleService.count(articleQueryWrapper);

            if (articleCount > 0) {
                //如果该分类下有文章，收集错误信息
                Category category = categoryService.getById(categoryId);
                String categoryName = category != null ? category.getName() : "ID:" + categoryId;
                if (!errorMsg.isEmpty()) {
                    errorMsg.append("; ");
                }
                errorMsg.append("【").append(categoryName).append("】下存在 ").append(articleCount).append(" 篇文章");
            }
        }

        //如果有分类下存在文章，返回所有错误信息
        if (!errorMsg.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, errorMsg.toString() + "，无法删除");
        }

        //根据分类id列表删除分类
        categoryService.removeByIds(idList);

        return ResponseResult.okResult();
    }

}
