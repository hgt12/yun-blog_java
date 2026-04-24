package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddLinkDto;
import com.mengze.domain.dto.LinkListDto;
import com.mengze.domain.dto.UpdateLinkDto;
import com.mengze.domain.vo.LinkFormDetailVo;
import com.mengze.domain.vo.LinkVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.AdminLinkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 后台友链管理接口
 */
@RestController
@RequestMapping("/links")
@Api(tags = "后台文章管理", description = "后台文章管理相关接口")
public class LinkController
{
    @Autowired
    private AdminLinkService adminLinkService;

    @GetMapping
    @SystemLog(businessName = "后台分页查询友链列表")
    @ApiOperation(value = "友链列表", notes = "分页查询友链列表", response = LinkVo.class, responseContainer = "List")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keywords", value = "关键字（友链名称）", dataType = "string", paramType = "query")
    })
    public ResponseResult<PageVo> getLinkList(Integer pageNum, Integer pageSize, @Valid LinkListDto linkListDto)
    {
        return adminLinkService.getLinkList(pageNum, pageSize, linkListDto);
    }

    @GetMapping("/{id}/form")
    @SystemLog(businessName = "获取友链表单数据")
    @ApiOperation(value = "获取友链表单数据", notes = "获取友链表单数据，用于编辑友链详情")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "友链id", dataType = "long", paramType = "path")
    })
    public ResponseResult<LinkFormDetailVo> getLinkFormDetail(@PathVariable Long id)
    {
        return adminLinkService.getLinkFormDetail(id);
    }

    @PostMapping
    @SystemLog(businessName = "新增友链")
    @ApiOperation(value = "新增友链接口", notes = "新增友链")
    public ResponseResult addLink(@Valid @RequestBody AddLinkDto addLinkDto)
    {
        return adminLinkService.addLink(addLinkDto);
    }

    @PutMapping("/{id}")
    @SystemLog(businessName = "修改友链")
    @ApiOperation(value = "修改友链接口", notes = "修改友链")
    @ApiImplicitParam(name = "id", value = "友链ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateLink(@PathVariable Long id, @Valid @RequestBody UpdateLinkDto updateLinkDto)
    {
        return adminLinkService.updateLink(id, updateLinkDto);
    }

    @DeleteMapping("/{ids}")
    @SystemLog(businessName = "删除友链(逻辑删除)")
    @ApiOperation(value = "删除友链接口（批量逻辑删除）", notes = "删除友链，支持批量删除")
    @ApiImplicitParam(name = "ids", value = "友链id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deleteLink(@PathVariable Long[] ids)
    {
        return adminLinkService.deleteLink(ids);
    }
}
