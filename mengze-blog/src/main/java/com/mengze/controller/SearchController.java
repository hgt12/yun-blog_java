package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.SearchService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

//搜索接口
@RestController
@Api(tags = "搜索", description = "搜索相关接口")
public class SearchController
{
    @Autowired
    private SearchService searchService;

    @GetMapping("/search")
    @SystemLog(businessName = "搜索文章")
    @ApiOperation(value = "搜索文章", notes = "根据关键词搜索文章（支持标题和内容搜索）", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keyword", value = "搜索关键词", required = true, dataType = "String"),
            @ApiImplicitParam(name = "pageNum", value = "页码，默认1", defaultValue = "1", dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量，默认10", defaultValue = "10", dataType = "Integer")
    })
    public ResponseResult search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize)
    {
        return searchService.search(pageNum, pageSize, keyword);
    }
}
