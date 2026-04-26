package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.MenuListDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.RoutersVo;
import com.mengze.service.SysMenuService;
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
 * 菜单相关接口
 */
@RestController
@RequestMapping("/menus")
@Api(tags = "菜单", description = "菜单相关接口")
public class SysMenuController
{
    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 获取当前登录用户可访问的前端路由树
     */
    @GetMapping("/getRoutes")
    @SystemLog(businessName = "获取当前登录用户可访问的前端路由树")
    @ApiOperation(value = "获取当前用户菜单路由树", notes = "获取当前登录用户可访问的前端路由树", response = RoutersVo.class)
    public ResponseResult getRoutes() {
        return sysMenuService.getRoutes();
    }

    @GetMapping
    @SystemLog(businessName = "菜单树列表")
    @ApiOperation(value = "菜单树列表", notes = "获取菜单树列表（不分页）", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "keywords", value = "菜单名称关键字", dataType = "string", paramType = "query")
    })
    public ResponseResult getMenuList(@Valid MenuListDto menuListDto)
    {
        return sysMenuService.getMenuList(menuListDto);
    }
}
