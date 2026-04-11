package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.TagListDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.TagVo;
import com.mengze.service.AdminTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台标签管理接口
 */
@RestController
@RequestMapping("/tags")
@Api(tags = "后台标签管理", description = "后台标签管理相关接口")
public class TagController
{
    @Autowired
    private AdminTagService adminTagService;

    @GetMapping
    @SystemLog(businessName = "后台分页查询标签列表")
    @ApiOperation(value = "标签列表", notes = "分页查询标签列表", response = TagVo.class, responseContainer = "List")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "name", value = "标签名称", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "remark", value = "备注", dataType = "string", paramType = "query")
    })
    public ResponseResult<PageVo> tagList(Integer pageNum, Integer pageSize, TagListDto tagListDto)
    {
        return adminTagService.tagList(pageNum, pageSize, tagListDto);
    }

    @GetMapping("/options")
    @SystemLog(businessName = "标签下拉选项")
    @ApiOperation(value = "标签下拉选项", notes = "标签分类下拉数据")
    public ResponseResult getTagOptions()
    {
        return adminTagService.getTagOptions();
    }
}
