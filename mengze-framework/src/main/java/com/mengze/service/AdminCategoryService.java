package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.CategoryOptionVo;

/**
 * 分类管理服务接口
 *
 * @author mengze
 */
public interface AdminCategoryService {

    ResponseResult<CategoryOptionVo> getCategoryOptions();
}
