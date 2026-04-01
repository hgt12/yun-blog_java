package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.entity.SysMenu;

import java.util.List;

/**
 * 菜单表(SysMenu)表服务接口
 *
 * @author makejava
 * @since 2026-03-27 19:07:41
 */
public interface SysMenuService extends IService<SysMenu> {

    List<String> selectPermByUserId(Long id);
}

