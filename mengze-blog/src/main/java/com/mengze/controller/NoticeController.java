package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.BlogNoticeDetailVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * 前台通知相关接口
 */
@RestController
@Api(tags = "通知", description = "前台通知相关接口")
public class NoticeController
{
    @Autowired
    private NoticeService noticeService;

    @GetMapping("/notice/list")
    @SystemLog(businessName = "获取通知列表接口")
    @ApiOperation(value = "获取通知列表接口", notes = "分页查询通知列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public ResponseResult<PageVo> getNoticeList(Integer pageNum, Integer pageSize)
    {
        return noticeService.getNoticeList(pageNum, pageSize);
    }

    @GetMapping("/notice/{id}")
    @SystemLog(businessName = "获取通知详情接口")
    @ApiOperation(value = "获取通知详情接口", notes = "获取通知的详细信息", response = PageVo.class)
    @ApiImplicitParam(name = "id", value = "通知id", dataType = "long", paramType = "path")
    public ResponseResult<BlogNoticeDetailVo> getNoticeDetail(@PathVariable Long id)
    {
        return noticeService.getNoticeDetail(id);
    }

}
