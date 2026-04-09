package com.mengze.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mengze.domain.entity.SysMenu;
import com.mengze.domain.vo.MenuVo;

import java.awt.*;
import java.util.List;

/**
 * 菜单表(SysMenu)表数据库访问层
 *
 * @author makejava
 * @since 2026-03-27 19:07:41
 */
public interface SysMenuMapper extends BaseMapper<SysMenu> {

    List<String> selectPermByUserId(Long userId);

    List<MenuVo> selectAllRouterMenu();

    List<MenuVo> selectRouterMenuTreeByUserId(Long userId);
}

