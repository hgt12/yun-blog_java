package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddMenuDto;
import com.mengze.domain.dto.MenuListDto;
import com.mengze.domain.dto.UpdateMenuDto;
import com.mengze.domain.vo.MenuFormDetailVo;
import com.mengze.domain.vo.MenuOptionVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.RoutersVo;
import com.mengze.service.SysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 菜单相关接口
 */
@RestController
@RequestMapping("/menus")
@Api(tags = "菜单", description = "菜单相关接口")
public class SysMenuController
{
    @Autowired
    private SysMenuService sysMenuService;

    @GetMapping("/getRoutes")
    @SystemLog(businessName = "获取当前登录用户可访问的前端路由树")
    @ApiOperation(value = "获取当前用户菜单路由树", notes = "获取当前登录用户可访问的前端路由树", response = RoutersVo.class)
    public ResponseResult getRoutes() {
        return sysMenuService.getRoutes();
    }

    @GetMapping
    @PreAuthorize("@ps.hasPermission('sys:menu:query')")
    @SystemLog(businessName = "菜单树列表")
    @ApiOperation(value = "菜单树列表", notes = "获取菜单树列表（不分页）", response = PageVo.class)
    public ResponseResult getMenuList(@Valid MenuListDto menuListDto)
    {
        return sysMenuService.getMenuList(menuListDto);
    }

    @GetMapping("/options")
    @SystemLog(businessName = "菜单下拉选项")
    @ApiOperation(value = "菜单下拉选项", notes = "获取菜单下拉数据", response = MenuOptionVo.class, responseContainer = "List")
    @ApiImplicitParam(name = "onlyParent", value = "是否只返回目录/菜单父节点", dataType = "boolean", paramType = "query")
    public ResponseResult getMenuOptions(Boolean onlyParent)
    {
        return sysMenuService.getMenuOptions(onlyParent);
    }

    @GetMapping("/{id}/form")
    @PreAuthorize("@ps.hasPermission('sys:menu:update')")
    @SystemLog(businessName = "获取菜单表单数据")
    @ApiOperation(value = "获取菜单表单数据", notes = "获取菜单表单数据，用于编辑菜单数据", response = MenuFormDetailVo.class)
    @ApiImplicitParam(name = "id", value = "菜单id", dataType = "long", paramType = "path")
    public ResponseResult<MenuFormDetailVo> getMenuFormDetail(@PathVariable Long id)
    {
        return sysMenuService.getMenuFormDetail(id);
    }

    @PostMapping
    @PreAuthorize("@ps.hasPermission('sys:menu:create')")
    @SystemLog(businessName = "新增菜单")
    @ApiOperation(value = "新增菜单接口", notes = "新增菜单", response = String.class)
    public ResponseResult addMenu(@Valid @RequestBody AddMenuDto addMenuDto)
    {
        return sysMenuService.addMenu(addMenuDto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@ps.hasPermission('sys:menu:update')")
    @SystemLog(businessName = "编辑菜单")
    @ApiOperation(value = "编辑菜单接口", notes = "编辑菜单", response = String.class)
    @ApiImplicitParam(name = "id", value = "菜单ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updateMenu(@PathVariable Long id, @Valid @RequestBody UpdateMenuDto updateMenuDto)
    {
        return sysMenuService.updateMenu(id, updateMenuDto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ps.hasPermission('sys:menu:delete')")
    @SystemLog(businessName = "删除菜单(逻辑删除)")
    @ApiOperation(value = "删除菜单接口", notes = "删除单个菜单（逻辑删除）", response = String.class)
    @ApiImplicitParam(name = "id", value = "菜单id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deleteMenu(@PathVariable Long id)
    {
        return sysMenuService.deleteMenu(id);
    }

}
