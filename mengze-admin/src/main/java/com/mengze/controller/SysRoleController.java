package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.dto.RoleListDto;
import com.mengze.domain.vo.RoleFormDetailVo;
import com.mengze.domain.vo.RoleOptionVo;
import com.mengze.service.SysRoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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
    })
    public ResponseResult<PageVo> getRoleList(Integer pageNum, Integer pageSize, @Valid RoleListDto roleListDto)
    {
        return sysRoleService.getRoleList(pageNum, pageSize, roleListDto);
    }

    @GetMapping("/options")
    @SystemLog(businessName = "角色下拉选项")
    @ApiOperation(value = "角色下拉选项", notes = "获取角色下拉数据", response = RoleOptionVo.class, responseContainer = "List")
    public ResponseResult getRoleOptions()
    {
        return sysRoleService.getRoleOptions();
    }

    @GetMapping("/{roleId}/menu-ids")
    @SystemLog(businessName = "角色菜单ID集合")
    @ApiOperation(value = "角色菜单ID集合", notes = "获取角色菜单ID集合", responseContainer = "List")
    public ResponseResult<List<Long>> getRoleMenuList(@PathVariable Long roleId)
    {
        return sysRoleService.getRoleMenuList(roleId);
    }

    @PutMapping("/{roleId}/menus")
    @SystemLog(businessName = "分配角色菜单")
    @ApiOperation(value = "分配角色菜单", notes = "给角色分配菜单（权限）", responseContainer = "List")
    public ResponseResult allocationRoleMenu(@PathVariable Long roleId, @RequestBody Long[] menuIds)
    {
        return sysRoleService.allocationRoleMenu(roleId, menuIds);
    }

    @GetMapping("/{id}/form")
    @SystemLog(businessName = "获取角色表单详情")
    @ApiOperation(value = "角色表单详情", notes = "获取角色表单详情", response = RoleFormDetailVo.class)
    public ResponseResult<RoleFormDetailVo> getRoleFormDetail(@PathVariable Long id)
    {
        return sysRoleService.getRoleFormDetail(id);
    }
}
