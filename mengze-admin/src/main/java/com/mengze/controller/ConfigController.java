package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddConfigDto;
import com.mengze.domain.dto.ConfigListDto;
import com.mengze.domain.dto.UpdateConfigDto;
import com.mengze.domain.vo.ConfigFormDetailVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.AdminConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("@ps.hasPermission('sys:config:query')")
    @SystemLog(businessName = "配置分页列表")
    @ApiOperation(value = "配置列表", notes = "配置分页列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
    })
    public ResponseResult<PageVo> getConfigList(Integer pageNum, Integer pageSize, @Valid ConfigListDto configListDto)
    {
        return adminConfigService.getConfigList(pageNum, pageSize, configListDto);
    }

    @GetMapping("/{id}/form")
    @PreAuthorize("@ps.hasPermission('sys:config:update')")
    @SystemLog(businessName = "获取配置表单数据")
    @ApiOperation(value = "获取配置表单数据", notes = "获取配置表单数据，用于编辑配置详情", response = ConfigFormDetailVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "配置id", dataType = "long", paramType = "path")
    })
    public ResponseResult<ConfigFormDetailVo> getConfigFormDetail(@PathVariable Long id)
    {
        return adminConfigService.getConfigFormDetail(id);
    }

    @PostMapping
    @PreAuthorize("@ps.hasPermission('sys:config:create')")
    @SystemLog(businessName = "新增配置")
    @ApiOperation(value = "新增配置接口", notes = "新增配置", response = String.class)
    public ResponseResult addConfig(@Valid @RequestBody AddConfigDto addConfigDto)
    {
        return adminConfigService.addConfig(addConfigDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ps.hasPermission('sys:config:update')")
    @SystemLog(businessName = "修改配置")
    @ApiOperation(value = "修改配置接口", notes = "修改配置", response = String.class)
    @ApiImplicitParam(name = "id", value = "配置ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateConfig(@PathVariable Long id, @Valid @RequestBody UpdateConfigDto updateConfigDto)
    {
        return adminConfigService.updateConfig(id, updateConfigDto);
    }

    @DeleteMapping("/{ids}")
    @PreAuthorize("@ps.hasPermission('sys:config:delete')")
    @SystemLog(businessName = "删除配置(逻辑删除)")
    @ApiOperation(value = "删除配置接口（批量逻辑删除）", notes = "删除配置，支持批量删除", response = String.class)
    @ApiImplicitParam(name = "ids", value = "配置id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deleteConfig(@PathVariable Long[] ids)
    {
        return adminConfigService.deleteConfig(ids);
    }
}
