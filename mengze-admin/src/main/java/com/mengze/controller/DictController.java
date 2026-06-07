package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.*;
import com.mengze.domain.vo.DictFormDetailVo;
import com.mengze.domain.vo.DictItemFormDetailVo;
import com.mengze.domain.vo.DictItemOptionVo;
import com.mengze.domain.vo.DictOptionVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.SysDictItemService;
import com.mengze.service.SysDictService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 字典接口
 */
@RestController
@RequestMapping("/dicts")
@Api(tags = "字典", description = "字典接口")
public class DictController
{
    @Autowired
    private SysDictService sysDictService;

    @Autowired
    private SysDictItemService sysDictItemService;

    @GetMapping
    @PreAuthorize("@ps.hasPermission('sys:dict:query')")
    @SystemLog(businessName = "分页查询字典列表")
    @ApiOperation(value = "字典列表", notes = "分页查询字典列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public ResponseResult getDictList(Integer pageNum, Integer pageSize, @Valid DictListDto dictListDto)
    {
        return sysDictService.getDictList(pageNum, pageSize, dictListDto);
    }

    //这个接口是在代码生成模块里使用的，但是目前前端项目中还没有代码生成模块，所以该接口预留在此，实际上是没有用上的
    @GetMapping("/options")
    @SystemLog(businessName = "字典下拉选项接口")
    @ApiOperation(value = "字典下拉选项接口", notes = "获取字典下拉数据", response = DictOptionVo.class, responseContainer = "List")
    public ResponseResult getDictOptions()
    {
        return sysDictService.getDictOptions();
    }

    @GetMapping("/{id}/form")
    @PreAuthorize("@ps.hasPermission('sys:dict:update')")
    @SystemLog(businessName = "获取字典表单数据接口")
    @ApiOperation(value = "获取字典表单数据接口", notes = "获取字典表单数据，用于编辑字典详情", response = DictFormDetailVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "字典id", dataType = "long", paramType = "path")
    })
    public ResponseResult<DictFormDetailVo> getDictFormDetail(@PathVariable Long id)
    {
        return sysDictService.getDictFormDetail(id);
    }

    @PostMapping
    @PreAuthorize("@ps.hasPermission('sys:dict:create')")
    @SystemLog(businessName = "新增字典")
    @ApiOperation(value = "新增字典接口", notes = "新增字典", response = String.class)
    public ResponseResult addDict(@Valid @RequestBody AddDictDto addDictDto)
    {
        return sysDictService.addDict(addDictDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ps.hasPermission('sys:dict:update')")
    @SystemLog(businessName = "修改字典接口")
    @ApiOperation(value = "编辑字典接口", notes = "编辑字典", response = String.class)
    @ApiImplicitParam(name = "id", value = "字典ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateDict(@PathVariable Long id, @Valid @RequestBody UpdateDictDto updateDictDto)
    {
        return sysDictService.updateDict(id, updateDictDto);
    }

    @DeleteMapping("/{ids}")
    @PreAuthorize("@ps.hasPermission('sys:dict:delete')")
    @SystemLog(businessName = "删除字典(逻辑删除)")
    @ApiOperation(value = "删除字典接口（批量逻辑删除）", notes = "删除字典，支持批量删除", response = String.class)
    @ApiImplicitParam(name = "ids", value = "字典id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deleteDict(@PathVariable Long[] ids)
    {
        return sysDictService.deleteDict(ids);
    }

    @GetMapping("/{dictCode}/items")
    @PreAuthorize("@ps.hasPermission('sys:dict:query')")
    @SystemLog(businessName = "分页查询字典项列表")
    @ApiOperation(value = "字典项列表", notes = "分页查询字典项列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "dictCode", value = "字典编码", dataType = "string", paramType = "path")
    })
    public ResponseResult getDictItemList(Integer pageNum, Integer pageSize, @PathVariable String dictCode, @Valid DictItemListDto dictItemListDto)
    {
        return sysDictItemService.getDictItemList(pageNum, pageSize, dictCode, dictItemListDto);
    }

    @GetMapping("/{dictCode}/items/options")
    @SystemLog(businessName = "字典项下拉选项接口")
    @ApiOperation(value = "字典项下拉选项接口", notes = "获取字典项下拉数据", response = DictItemOptionVo.class, responseContainer = "List")
    @ApiImplicitParam(name = "dictCode", value = "字典编码", dataType = "string", paramType = "path")
    public ResponseResult getDictItemOptions(@PathVariable String dictCode)
    {
        return sysDictItemService.getDictItemOptions(dictCode);
    }

    @GetMapping("/{dictCode}/items/{id}/form")
    @PreAuthorize("@ps.hasPermission('sys:dict:update')")
    @SystemLog(businessName = "获取字典项表单数据接口")
    @ApiOperation(value = "获取字典项表单数据接口", notes = "获取字典项表单数据，用于编辑字典项详情", response = DictItemFormDetailVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "字典id", dataType = "long", paramType = "path"),
            @ApiImplicitParam(name = "dictCode", value = "字典编码", dataType = "string", paramType = "path")
    })
    public ResponseResult<DictItemFormDetailVo> getDictItemFormDetail(@PathVariable Long id, @PathVariable String dictCode)
    {
        return sysDictItemService.getDictItemFormDetail(id, dictCode);
    }

    @PostMapping("/{dictCode}/items")
    @PreAuthorize("@ps.hasPermission('sys:dict:create')")
    @SystemLog(businessName = "新增字典项")
    @ApiOperation(value = "新增字典项接口", notes = "新增字典项", response = String.class)
    public ResponseResult addDictItem(@PathVariable String dictCode, @Valid @RequestBody AddDictItemDto addDictItemDto)
    {
        return sysDictItemService.addDictItem(dictCode, addDictItemDto);
    }

    @PutMapping("/{dictCode}/items/{id}")
    @PreAuthorize("@ps.hasPermission('sys:dict:update')")
    @SystemLog(businessName = "修改字典项接口")
    @ApiOperation(value = "编辑字典项接口", notes = "编辑字典项", response = String.class)
    @ApiImplicitParam(name = "id", value = "字典项ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateDictItem(@PathVariable Long id, @PathVariable String dictCode, @Valid @RequestBody UpdateDictItemDto updateDictItemDto)
    {
        return sysDictItemService.updateDictItem(id, dictCode, updateDictItemDto);
    }

    @DeleteMapping("/{dictCode}/items/{ids}")
    @PreAuthorize("@ps.hasPermission('sys:dict:delete')")
    @SystemLog(businessName = "删除字典项(逻辑删除)")
    @ApiOperation(value = "删除字典项接口（批量逻辑删除）", notes = "删除字典项，支持批量删除", response = String.class)
    @ApiImplicitParam(name = "ids", value = "字典项id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deleteDictItem(@PathVariable Long[] ids)
    {
        return sysDictItemService.deleteDictItem(ids);
    }

}
