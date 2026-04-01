package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.mapper.SysMenuMapper;
import com.mengze.domain.entity.SysMenu;
import com.mengze.service.SysMenuService;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单表(SysMenu)表服务实现类
 *
 * @author makejava
 * @since 2026-03-27 19:07:41
 */
@Service("sysMenuService")
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService
{

    @Override
    public List<String> selectPermByUserId(Long id)
    {
        //如果是管理员，则返回所有权限
        if (id == 1L){
            LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SysMenu :: getType, SystemConstants.MENU_TYPE_M,SystemConstants.MENU_TYPE_B);
            queryWrapper.eq(SysMenu :: getStatus, SystemConstants.STATUS_NORMAL);
            List<SysMenu> menuList = list(queryWrapper);
            List<String> perms = menuList.stream()
                    .map(SysMenu::getPerm)
                    .collect(Collectors.toList());
            return perms;

        }

        //如果不是管理员，则返回其对应的权限

        return getBaseMapper().selectPermByUserId(id);
    }
}

