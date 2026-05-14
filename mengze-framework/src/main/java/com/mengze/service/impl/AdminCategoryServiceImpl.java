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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    public ResponseResult getCategoryList(Integer pageNum, Integer pageSize, CategoryListDto categoryListDto)
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
        List<AdminCategoryListVo> categoryListVos = BeanCopyUtils.copyBeanList(categoryList, AdminCategoryListVo.class);

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
    public ResponseResult<CategoryFormDetailVo> getCategoryFormDetail(Long id)
    {
        //根据分类id 查询分类
        Category category = categoryService.getById(id);
        if (category == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该分类不存在！");
        }

        //转换成Vo
        CategoryFormDetailVo categoryFormDetailVo = BeanCopyUtils.copyBean(category, CategoryFormDetailVo.class);

        //返回
        return ResponseResult.okResult(categoryFormDetailVo);
    }

    //新增分类
    @Override
    public ResponseResult addCategory(AddCategoryDto addCategoryDto)
    {
        // 检查分类名称是否已存在
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, addCategoryDto.getName());
        long count = categoryService.count(queryWrapper);
        
        if (count > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "分类名称已存在，请勿重复添加！");
        }
        
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

        //检查分类是否存在
        Category oldCategory = categoryService.getById(id);
        if (oldCategory == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该分类不存在！");
        }

        // 检查分类名称是否与其他分类重复（排除自己）
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Category::getName, updateCategoryDto.getName())
                    .ne(Category::getId, id); // 排除当前分类
        long count = categoryService.count(queryWrapper);
        
        if (count > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "分类名称已存在，请修改后重试！");
        }

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

        return ResponseResult.okResult(Objects.requireNonNullElse(msg, "修改成功"));

    }

    //批量删除分类（逻辑删除）
    @Override
    public ResponseResult deleteCategory(Long[] ids)
    {
        // 参数校验
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的分类");
        }

        //获取分类id列表
        List<Long> idList = List.of(ids);

        //检查分类是否存在
        List<Category> categoriesToDelete = categoryService.listByIds(idList);
        if (categoriesToDelete.size() != ids.length) {
            //找出不存在的 分类id
            Set<Long> existIds = categoriesToDelete.stream()
                    .map(Category::getId)
                    .collect(Collectors.toSet());

            List<Long> notExistIds = idList.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败！！原因：以下分类 id 不存在：" + notExistIds);
        }

        StringBuilder errorMsg = new StringBuilder();

        //收集所有有文章的分类信息并检查每个分类下是否有文章
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
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, errorMsg + "，无法删除");
        }

        //根据分类id列表删除分类
        categoryService.removeByIds(idList);

        return ResponseResult.okResult();
    }

}
