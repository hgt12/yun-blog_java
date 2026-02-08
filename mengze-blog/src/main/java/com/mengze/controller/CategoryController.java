package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//分类列表接口
@RestController
@RequestMapping("/category")
public class CategoryController
{
    @Autowired
    private CategoryService categoryService;

    @SystemLog(businessName = "查询分类列表")
    @GetMapping("/getCategoryList")
    public ResponseResult getCategoryList()
    {
        return categoryService.getCategoryList();
    }
}
