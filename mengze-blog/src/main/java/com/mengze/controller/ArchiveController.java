package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.service.ArchiveService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

//归档接口
@RestController
@RequestMapping("/archive")
@Api(tags = "归档", description = "归档相关接口")
public class ArchiveController
{
    @Autowired
    private ArchiveService archiveService;

    /**
     * 获取归档列表（分页）
     */
    @SystemLog(businessName = "查询归档列表")
    @GetMapping("/getArchiveList")
    @ApiOperation(value = "归档列表", notes = "获取归档列表（分页，按创建时间倒序）")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", required = true, dataType = "Integer"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", defaultValue = "10", dataType = "Integer")
    })
    public ResponseResult getArchiveList(
            @RequestParam Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return archiveService.getArchiveList(pageNum, pageSize);
    }

    /**
     * 按年份、月份获取文章
     */
    @SystemLog(businessName = "根据年月查询文章列表")
    @GetMapping("/{year}/{month}")
    @ApiOperation(value = "根据年月获取文章列表", notes = "根据年月获取该月的文章列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "year",value = "年份"),
            @ApiImplicitParam(name = "month",value = "月份"),
            @ApiImplicitParam(name = "pageNum",value = "页号"),
            @ApiImplicitParam(name = "pageSize",value = "每页数量")
    })
    public ResponseResult getArticlesByYearMonth(
            @PathVariable("year") Integer year,
            @PathVariable("month") Integer month,
            @RequestParam Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return archiveService.getArticlesByYearMonth(year, month, pageNum, pageSize);
    }
}
