package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.service.ConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前台系统配置接口
 */
@RestController
@RequestMapping("/config")
@Api(tags = "前台系统配置")
public class ConfigController
{
    @Autowired
    private ConfigService configService;

    @GetMapping("/getBlogConfig")
    @SystemLog(businessName = "获取前台系统配置接口")
    @ApiOperation(value = "获取前台系统配置接口", notes = "获取博客前台的基本配置")
    public ResponseResult getBlogConfig()
    {
        return configService.getBlogConfig();
    }
}
