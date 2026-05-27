package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.NoticeListDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.SysNoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 通知相关接口
 */
@RestController
@RequestMapping("/notices")
@Api(tags = "通知", description = "通知相关接口")
public class NoticeController
{
    @Autowired
    private SysNoticeService sysNoticeService;

    @GetMapping
    @PreAuthorize("@ps.hasPermission('sys:notice:query')")
    @SystemLog(businessName = "获取通知列表接口")
    @ApiOperation(value = "获取通知列表接口", notes = "分页查询通知列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public ResponseResult<PageVo> getNoticeList(Integer pageNum, Integer pageSize, @Valid NoticeListDto noticeListDto)
    {
        return sysNoticeService.getNoticeList(pageNum, pageSize, noticeListDto);
    }
}
