package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.RoutersVo;
import com.mengze.domain.vo.SiteInfoVo;
import com.mengze.service.SysMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
