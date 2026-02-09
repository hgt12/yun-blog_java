package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.LinkVo;
import com.mengze.service.LinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//友链列表接口
@RestController
@RequestMapping("/links")
@Api(tags = "友链", description = "友情链接相关接口")
public class LinkController
{
    @Autowired
    private LinkService linkService;

    @GetMapping("getAllLink")
    @SystemLog(businessName = "查询友链列表")
    @ApiOperation(value = "友链列表", notes = "获取所有友情链接列表", response = LinkVo.class, responseContainer = "List")
    public ResponseResult getAllLink()
    {
        return linkService.getAllLink();
    }
}
