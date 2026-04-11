package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.CategoryOptionVo;
import com.mengze.service.AdminCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 分类管理接口
 */
@RestController
@RequestMapping("/categories")
@Api(tags = "分类管理", description = "分类管理相关接口")
public class CategoryController
{
    @Autowired
    private AdminCategoryService adminCategoryService;

    @GetMapping("/options")
    @SystemLog(businessName = "分类下拉选项")
    @ApiOperation(value = "分类下拉选项", notes = "获取分类下拉数据")
    public ResponseResult getCategoryOptions()
    {
        return adminCategoryService.getCategoryOptions();
    }
}
