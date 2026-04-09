package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.MenuVo;
import com.mengze.domain.vo.RoutersVo;
import com.mengze.mapper.SysMenuMapper;
import com.mengze.domain.entity.SysMenu;
import com.mengze.service.SysMenuService;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    @Autowired
    private SysMenuService sysMenuService;

    @Override
    public List<String> selectPermByUserId(Long id)
    {
        //如果是管理员，则返回所有权限
        if (SecurityUtils.isAdmin()){
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

    /**
     * 获取当前登录用户可访问的前端路由树
     */
    @Override
    public ResponseResult getRoutes()
    {
        //查询用户id
        Long userId = SecurityUtils.getUserId();

        //查询menu
        SysMenuMapper menuMapper = getBaseMapper();
        List<MenuVo> menus;
        //判断是否是管理员
        if (SecurityUtils.isAdmin()){
            //如果是，返回符合要求的menu
            menus = menuMapper.selectAllRouterMenu();
        }else {
            //否则就返回当前用户所具有的menu 类型为菜单和目录类型
            menus = menuMapper.selectRouterMenuTreeByUserId(userId);
        }
        
        //构建tree
        //先找第一层的菜单，然后再找他的子菜单设置到children字段中
        List<MenuVo> menuTree = builderMenuTree(menus, 0L);

        //封装数据返回 - 使用 RoutersVo 包装菜单数组
        return ResponseResult.okResult(new RoutersVo(menuTree));
    }

    //创建菜单树
    private List<MenuVo> builderMenuTree(List<MenuVo> menus, Long parentId)
    {
        List<MenuVo> menuTree = menus.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .map(menu -> menu.setChildren(getChildren(menu, menus)))
                .collect(Collectors.toList());
        return menuTree;
    }

    /**
     * 获取传入参数的 子菜单（menu）集合
     * @param menu
     * @param menus
     * @return
     */
    private List<MenuVo> getChildren(MenuVo menu, List<MenuVo> menus)
    {
        List<MenuVo> childrenList = menus.stream()
                .filter(m -> m.getParentId().equals(menu.getId()))
                .map(m -> m.setChildren(getChildren(m, menus)))
                .collect(Collectors.toList());
        return childrenList;
    }
}

