package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddMenuDto;
import com.mengze.domain.dto.MenuListDto;
import com.mengze.domain.dto.UpdateMenuDto;
import com.mengze.domain.entity.SysMenu;
import com.mengze.domain.vo.MenuFormDetailVo;

import java.util.List;

/**
 * 菜单表(SysMenu)表服务接口
 *
 * @author makejava
 * @since 2026-03-27 19:07:41
 */
public interface SysMenuService extends IService<SysMenu> {

    List<String> selectPermByUserId(Long id);

    ResponseResult getRoutes();

    ResponseResult getMenuList(MenuListDto menuListDto);

    ResponseResult getMenuOptions(Boolean onlyParent);

    ResponseResult<MenuFormDetailVo> getMenuFormDetail(Long id);

    ResponseResult addMenu(AddMenuDto addMenuDto);

    ResponseResult updateMenu(Long id, UpdateMenuDto updateMenuDto);
}

