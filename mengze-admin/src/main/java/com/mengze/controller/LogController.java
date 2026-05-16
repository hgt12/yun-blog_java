package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LogListDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.VisitLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 日志管理接口
 */
@RestController
@RequestMapping("/logs")
@Api(tags = "日志管理", description = "日志管理相关接口")
public class LogController
{
    @Autowired
    private VisitLogService visitLogService;

    @GetMapping
    @SystemLog(businessName = "分页查询日志列表")
    @ApiOperation(value = "日志列表", notes = "分页查询日志列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public ResponseResult<PageVo> getLogList(Integer pageNum, Integer pageSize, @Valid LogListDto logListDto)
    {
        return visitLogService.getLogList(pageNum, pageSize, logListDto);
    }


}
