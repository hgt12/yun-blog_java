package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddDeptDto;
import com.mengze.domain.dto.DeptListDto;
import com.mengze.domain.dto.UpdateDeptDto;
import com.mengze.domain.vo.DeptFormDetailVo;
import com.mengze.domain.vo.DeptOptionVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.SysDeptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 部门接口
 */
@RestController
@RequestMapping("/depts")
@Api(tags = "部门", description = "部门接口")
public class DeptController
{
    @Autowired
    private SysDeptService sysDeptService;

    @GetMapping
    @PreAuthorize("@ps.hasPermission('sys:dept:query')")
    @SystemLog(businessName = "部门树列表")
    @ApiOperation(value = "部门树列表", notes = "获取部门树列表（不分页）", response = PageVo.class)
    public ResponseResult getDeptList(@Valid DeptListDto deptListDto)
    {
        return sysDeptService.getDeptList(deptListDto);
    }

    @GetMapping("/options")
    @SystemLog(businessName = "部门下拉选项")
    @ApiOperation(value = "部门下拉选项", notes = "获取部门下拉数据", response = DeptOptionVo.class, responseContainer = "List")
    public ResponseResult getDeptOptions()
    {
        return sysDeptService.getDeptOptions();
    }

    @GetMapping("/{id}/form")
    @PreAuthorize("@ps.hasPermission('blog:dept:update')")
    @SystemLog(businessName = "获取部门表单数据接口")
    @ApiOperation(value = "获取部门表单数据接口", notes = "获取部门表单数据，用于编辑部门详情", response = DeptFormDetailVo.class)
    @ApiImplicitParam(name = "id", value = "部门id", dataType = "long", paramType = "path")
    public ResponseResult<DeptFormDetailVo> getDeptFormDetail(@PathVariable Long id)
    {
        return sysDeptService.getDeptFormDetail(id);
    }

    @PostMapping
    @PreAuthorize("@ps.hasPermission('blog:dept:create')")
    @SystemLog(businessName = "新增部门接口")
    @ApiOperation(value = "新增部门接口", notes = "新增部门", response = String.class)
    public ResponseResult addDept(@Valid @RequestBody AddDeptDto addDeptDto)
    {
        return sysDeptService.addDept(addDeptDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ps.hasPermission('blog:dept:update')")
    @SystemLog(businessName = "编辑部门接口")
    @ApiOperation(value = "编辑部门接口", notes = "编辑部门", response = String.class)
    @ApiImplicitParam(name = "id", value = "部门ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateDept(@PathVariable Long id, @Valid @RequestBody UpdateDeptDto updateDeptDto)
    {
        return sysDeptService.updateDept(id, updateDeptDto);
    }
}
