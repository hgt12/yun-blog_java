package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddDictDto;
import com.mengze.domain.dto.DictListDto;
import com.mengze.domain.dto.UpdateDictDto;
import com.mengze.domain.vo.DictFormDetailVo;
import com.mengze.domain.vo.DictOptionVo;
import com.mengze.domain.vo.PageVo;
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

    @GetMapping
    @PreAuthorize("@ps.hasPermission('blog:dict:query')")
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
    @PreAuthorize("@ps.hasPermission('blog:dict:update')")
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
    @PreAuthorize("@ps.hasPermission('blog:dict:create')")
    @SystemLog(businessName = "新增字典")
    @ApiOperation(value = "新增字典接口", notes = "新增字典", response = String.class)
    public ResponseResult addDict(@Valid @RequestBody AddDictDto addDictDto)
    {
        return sysDictService.addDict(addDictDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ps.hasPermission('blog:dict:update')")
    @SystemLog(businessName = "修改字典接口")
    @ApiOperation(value = "编辑字典接口", notes = "编辑字典", response = String.class)
    @ApiImplicitParam(name = "id", value = "字典ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateDict(@PathVariable Long id, @Valid @RequestBody UpdateDictDto updateDictDto)
    {
        return sysDictService.updateDict(id, updateDictDto);
    }
}
