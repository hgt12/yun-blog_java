package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Category;

/**
 * 分类表(Category)表服务接口
 *
 * @author makejava
 * @since 2025-11-26 16:28:19
 */
public interface CategoryService extends IService<Category> {

    ResponseResult getCategoryList();
}

