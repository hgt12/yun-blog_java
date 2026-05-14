package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.dto.RoleListDto;
import com.mengze.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 角色相关接口
 */
@RestController
@RequestMapping("/roles")
@Api(tags = "角色", description = "角色相关接口")
public class SysRoleController
{
    @Autowired
    private SysRoleService sysRoleService;

    @GetMapping
    @SystemLog(businessName = "后台分页查询角色列表")
    @ApiOperation(value = "角色列表", notes = "分页查询角色列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keywords", value = "关键字（角色名称）", dataType = "string", paramType = "query")
    })
    public ResponseResult<PageVo> getRoleList(Integer pageNum, Integer pageSize, @Valid RoleListDto roleListDto)
    {
        return sysRoleService.getRoleList(pageNum, pageSize, roleListDto);
    }
}
