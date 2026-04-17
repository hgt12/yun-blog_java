package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddCategoryDto;
import com.mengze.domain.dto.CategoryListDto;
import com.mengze.domain.dto.UpdateCategoryDto;
import com.mengze.domain.vo.CategoryFormDetailVo;
import com.mengze.domain.vo.CategoryOptionVo;

/**
 * 分类管理服务接口
 *
 * @author mengze
 */
public interface AdminCategoryService {

    ResponseResult<CategoryOptionVo> getCategoryOptions();

    ResponseResult categoryList(Integer pageNum, Integer pageSize, CategoryListDto categoryListDto);

    ResponseResult<CategoryFormDetailVo> categoryFormDetail(Long id);

    ResponseResult addCategory(AddCategoryDto addCategoryDto);

    ResponseResult updateCategory(Long id, UpdateCategoryDto updateCategoryDto);

    ResponseResult deleteCategory(Long[] ids);
}
