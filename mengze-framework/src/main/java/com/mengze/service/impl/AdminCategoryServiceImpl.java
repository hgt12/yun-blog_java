package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Category;
import com.mengze.domain.vo.CategoryOptionVo;
import com.mengze.service.AdminCategoryService;
import com.mengze.service.CategoryService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
