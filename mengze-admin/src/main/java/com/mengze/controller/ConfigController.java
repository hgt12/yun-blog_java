package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.ConfigListDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.AdminConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 系统配置接口
 */
@RestController
@RequestMapping("/configs")
@Api(tags = "系统配置", description = "系统配置接口")
public class ConfigController
{
    @Autowired
    AdminConfigService adminConfigService;

    @GetMapping
    @SystemLog(businessName = "配置分页列表")
    @ApiOperation(value = "配置列表", notes = "配置分页列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
    })
    public ResponseResult<PageVo> getConfigList(Integer pageNum, Integer pageSize, @Valid @RequestBody ConfigListDto configListDto)
    {
        return adminConfigService.getConfigList(pageNum, pageSize, configListDto);
    }
}
