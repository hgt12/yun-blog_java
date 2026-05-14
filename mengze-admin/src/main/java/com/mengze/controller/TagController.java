package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddTagDto;
import com.mengze.domain.dto.TagListDto;
import com.mengze.domain.dto.UpdateTagDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.TagFormDetailVo;
import com.mengze.domain.vo.TagOptionVo;
import com.mengze.service.AdminTagService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    @ApiOperation(value = "标签列表", notes = "分页查询标签列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
    })
    public ResponseResult<PageVo> getTagList(Integer pageNum, Integer pageSize, @Valid TagListDto tagListDto)
    {
        return adminTagService.getTagList(pageNum, pageSize, tagListDto);
    }

    @GetMapping("/options")
    @SystemLog(businessName = "标签下拉选项")
    @ApiOperation(value = "标签下拉选项", notes = "标签分类下拉数据", response = TagOptionVo.class, responseContainer = "List")
    public ResponseResult getTagOptions()
    {
        return adminTagService.getTagOptions();
    }

    @GetMapping("/{id}/form")
    @SystemLog(businessName = "获取标签表单数据")
    @ApiOperation(value = "获取标签表单数据", notes = "获取标签表单数据，用于编辑标签详情", response = TagFormDetailVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "标签id", dataType = "long", paramType = "path")
    })
    public ResponseResult<TagFormDetailVo> getTagFormDetail(@PathVariable Long id)
    {
        return adminTagService.getTagFormDetail(id);
    }


    @PostMapping
    @SystemLog(businessName = "新增标签")
    @ApiOperation(value = "新增标签接口", notes = "新增标签", response = String.class)
    public ResponseResult addTag(@Valid @RequestBody AddTagDto addTagDto)
    {
        return adminTagService.addTag(addTagDto);
    }

    @PutMapping("/{id}")
    @SystemLog(businessName = "修改标签")
    @ApiOperation(value = "修改标签接口", notes = "修改标签", response = String.class)
    @ApiImplicitParam(name = "id", value = "标签ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateTag(@PathVariable Long id, @Valid @RequestBody UpdateTagDto updateTagDto)
    {
        return adminTagService.updateTag(id, updateTagDto);
    }

    @DeleteMapping("/{ids}")
    @SystemLog(businessName = "删除标签(逻辑删除)")
    @ApiOperation(value = "删除标签接口（批量逻辑删除）", notes = "删除标签，支持批量删除", response = String.class)
    @ApiImplicitParam(name = "ids", value = "标签id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deleteTag(@PathVariable Long[] ids)
    {
        return adminTagService.deleteTag(ids);
    }
}
