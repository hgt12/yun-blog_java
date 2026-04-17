package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.CategoryListDto;
import com.mengze.domain.vo.ArticleListVo;
import com.mengze.domain.vo.CategoryFormDetailVo;
import com.mengze.domain.vo.CategoryVo;
import com.mengze.service.AdminCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping
    @SystemLog(businessName = "分页查询分类列表")
    @ApiOperation(value = "分类列表", notes = "分页查询分类列表", response = CategoryVo.class, responseContainer = "List")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keywords", value = "搜索关键字", dataType = "string", paramType = "query")
    })
    public ResponseResult categoryList(Integer pageNum, Integer pageSize, CategoryListDto categoryListDto)
    {
        return adminCategoryService.categoryList(pageNum, pageSize, categoryListDto);
    }

    @GetMapping("/options")
    @SystemLog(businessName = "分类下拉选项")
    @ApiOperation(value = "分类下拉选项", notes = "获取分类下拉数据")
    public ResponseResult getCategoryOptions()
    {
        return adminCategoryService.getCategoryOptions();
    }

    @GetMapping("/{id}/form")
    @SystemLog(businessName = "获取分类表单数据")
    @ApiOperation(value = "获取分类表单数据", notes = "获取分类表单数据，用于编辑分类详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "分类id", dataType = "long", paramType = "path")
    })
    public ResponseResult<CategoryFormDetailVo> categoryFormDetail(@PathVariable Long id)
    {
        return adminCategoryService.categoryFormDetail(id);
    }
}
