package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.*;
import com.mengze.domain.vo.CategoryFormDetailVo;
import com.mengze.domain.vo.CategoryOptionVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.AdminCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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

    @GetMapping
    @SystemLog(businessName = "分页查询分类列表")
    @ApiOperation(value = "分类列表", notes = "分页查询分类列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keywords", value = "搜索关键字", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "状态", dataType = "string", paramType = "query")
    })
    public ResponseResult getCategoryList(Integer pageNum, Integer pageSize, @Valid CategoryListDto categoryListDto)
    {
        return adminCategoryService.getCategoryList(pageNum, pageSize, categoryListDto);
    }

    @GetMapping("/options")
    @SystemLog(businessName = "分类下拉选项")
    @ApiOperation(value = "分类下拉选项", notes = "获取分类下拉数据", response = CategoryOptionVo.class, responseContainer = "List")
    public ResponseResult getCategoryOptions()
    {
        return adminCategoryService.getCategoryOptions();
    }

    @GetMapping("/{id}/form")
    @SystemLog(businessName = "获取分类表单数据")
    @ApiOperation(value = "获取分类表单数据", notes = "获取分类表单数据，用于编辑分类详情", response = CategoryFormDetailVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "分类id", dataType = "long", paramType = "path")
    })
    public ResponseResult<CategoryFormDetailVo> getCategoryFormDetail(@PathVariable Long id)
    {
        return adminCategoryService.getCategoryFormDetail(id);
    }

    @PostMapping
    @SystemLog(businessName = "新增分类")
    @ApiOperation(value = "新增分类接口", notes = "新增分类", response = String.class)
    public ResponseResult addCategory(@Valid @RequestBody AddCategoryDto addCategoryDto)
    {
        return adminCategoryService.addCategory(addCategoryDto);
    }

    @PutMapping("/{id}")
    @SystemLog(businessName = "编辑分类")
    @ApiOperation(value = "编辑分类接口", notes = "编辑分类", response = String.class)
    @ApiImplicitParam(name = "id", value = "分类ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryDto updateCategoryDto)
    {
        return adminCategoryService.updateCategory(id, updateCategoryDto);
    }

    @DeleteMapping("/{ids}")
    @SystemLog(businessName = "删除分类(逻辑删除)")
    @ApiOperation(value = "删除分类接口（批量逻辑删除）", notes = "删除分类，支持批量删除", response = String.class)
    @ApiImplicitParam(name = "ids", value = "分类id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deleteCategory(@PathVariable Long[] ids)
    {
        return adminCategoryService.deleteCategory(ids);
    }
}
