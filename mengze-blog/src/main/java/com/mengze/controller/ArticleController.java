package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.ArticleDetailVo;
import com.mengze.domain.vo.HotArticleListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.ArticleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//与文章相关的接口
@RestController
@RequestMapping("/article")//对应本地链接http://localhost:1314/article/
@Api(tags = "文章", description = "文章相关接口")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    //分页查询文章列表（页码，每页大小参数）
    @GetMapping("/articleList")
    @SystemLog(businessName = "分页查询文章列表")
    @ApiOperation(value = "文章列表", notes = "分页查询文章列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量")
    })
    public  ResponseResult articleList(Integer pageNum, Integer pageSize)
    {
        return articleService.articleList(pageNum, pageSize);
    }

    //文章详情接口
    @GetMapping("/{id}")
    @SystemLog(businessName = "查询文章详情")
    @ApiOperation(value = "文章详情", notes = "根据文章id查询文章详情", response = ArticleDetailVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "文章id")
    })
    public  ResponseResult getArticleDetail(@PathVariable("id") Long id)
    {
        return articleService.getArticleDetail(id);
    }

    //根据分类id查询文章列表
    @GetMapping("/articleListByCategoryId/{categoryId}")
    @SystemLog(businessName = "根据分类ID查询文章列表")
    @ApiOperation(value = "根据分类查询文章", notes = "根据分类id分页查询文章列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "categoryId", value = "分类id"),
            @ApiImplicitParam(name = "pageNum", value = "页号"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量")
    })
    public ResponseResult articleListByCategoryId(@PathVariable("categoryId") Long categoryId, Integer pageNum, Integer pageSize)
    {
        return articleService.articleListByCategoryId(categoryId, pageNum, pageSize);
    }

    //根据标签id查询文章列表
    @GetMapping("/articleListByTagId/{tagId}")
    @SystemLog(businessName = "根据标签ID查询文章列表")
    @ApiOperation(value = "根据标签查询文章", notes = "根据标签id分页查询文章列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "tagId", value = "标签id", dataType = "long", paramType = "path"),
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
    })
    public ResponseResult articleListByTagId(@PathVariable("tagId") Long tagId, Integer pageNum, Integer pageSize)
    {
        return articleService.articleListByTagId(tagId, pageNum, pageSize);
    }

    // 预览量接口
    @PutMapping("/viewCount/{id}")
    @SystemLog(businessName = "根据根据文章id获取预览量")
    @ApiOperation(value = "预览量", notes = "根据根据文章id获取预览量", response = String.class)
    @ApiImplicitParam(name = "id", value = "id", dataType = "long", paramType = "path")
    public ResponseResult viewCount(@PathVariable("id") Long id)
    {
        return articleService.viewCount(id);
    }
}
