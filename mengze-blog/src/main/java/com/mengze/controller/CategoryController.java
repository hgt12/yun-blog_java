package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.CategoryVo;
import com.mengze.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//分类相关接口
@RestController
@RequestMapping("/category")
@Api(tags = "分类", description = "文章分类相关接口")
public class CategoryController
{
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/getCategoryList")
    @SystemLog(businessName = "查询分类列表")
    @ApiOperation(value = "分类列表", notes = "获取所有文章分类列表", response = CategoryVo.class, responseContainer = "List")
    public ResponseResult getCategoryList()
    {
        return categoryService.getCategoryList();
    }
}
