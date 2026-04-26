package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.MenuListDto;
import com.mengze.domain.vo.MenuRoutesVo;
import com.mengze.domain.vo.MenuListVo;
import com.mengze.domain.vo.RoutersVo;
import com.mengze.mapper.SysMenuMapper;
import com.mengze.domain.entity.SysMenu;
import com.mengze.service.SysMenuService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
            queryWrapper.eq(SysMenu :: getVisible, SystemConstants.VISIBLE);
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
        List<MenuRoutesVo> menus;
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
        List<MenuRoutesVo> menuTree = builderMenuRoutesTree(menus, 0L);

        //封装数据返回 - 使用 RoutersVo 包装菜单数组
        return ResponseResult.okResult(new RoutersVo(menuTree));
    }

    //获取菜单列表
    @Override
    public ResponseResult getMenuList(MenuListDto menuListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();

        //如果有关键字，则进行模糊查询
        if (StringUtils.hasText(menuListDto.getKeywords())) {
            queryWrapper.like(SysMenu::getName, menuListDto.getKeywords());
        }

        //只查询可见的菜单
        queryWrapper.eq(SysMenu::getVisible, SystemConstants.VISIBLE);

        //按排序字段升序排列
        queryWrapper.orderByAsc(SysMenu::getSort);

        //查询所有符合条件的菜单
        List<SysMenu> menuList = list(queryWrapper);

        //转换为 MenuListVo 列表
        List<MenuListVo> menuListVo = BeanCopyUtils.copyBeanList(menuList, MenuListVo.class);

        //构建菜单树
        List<MenuListVo> menuTree = builderMenuListTree(menuListVo, 0L);

        //返回结果
        return ResponseResult.okResult(menuTree);
    }


    //创建MenuList的菜单树
    private List<MenuListVo> builderMenuListTree(List<MenuListVo> menuListVos, Long parentId)
    {
        List<MenuListVo> menuTree = menuListVos.stream()
                .filter(menuListVo -> menuListVo.getParentId().equals(parentId))
                .map(menuListVo -> menuListVo.setChildren(getChildrenByMenuList(menuListVo, menuListVos)))
                .collect(Collectors.toList());
        return menuTree;
    }

    //获取传入参数的 子菜单（MenuList）集合
    private List<MenuListVo> getChildrenByMenuList(MenuListVo menuListVo, List<MenuListVo> menuListVos)
    {
        List<MenuListVo> childrenList = menuListVos.stream()
                .filter(m -> m.getParentId().equals(menuListVo.getId()))
                .map(m -> m.setChildren(getChildrenByMenuList(m, menuListVos)))
                .collect(Collectors.toList());
        return childrenList;
    }

    //创建MenuRoutes的菜单树
    private List<MenuRoutesVo> builderMenuRoutesTree(List<MenuRoutesVo> menuRoutesVos, Long parentId)
    {
        List<MenuRoutesVo> menuTree = menuRoutesVos.stream()
                .filter(menu -> menu.getParentId().equals(parentId))
                .map(menu -> menu.setChildren(getChildrenByMenuRoutes(menu, menuRoutesVos)))
                .collect(Collectors.toList());
        return menuTree;
    }

    //获取传入参数的 子菜单（MenuRoutes）集合
    private List<MenuRoutesVo> getChildrenByMenuRoutes(MenuRoutesVo menuRoutesVo, List<MenuRoutesVo> menuRoutesVos)
    {
        List<MenuRoutesVo> childrenList = menuRoutesVos.stream()
                .filter(m -> m.getParentId().equals(menuRoutesVo.getId()))
                .map(m -> m.setChildren(getChildrenByMenuRoutes(m, menuRoutesVos)))
                .collect(Collectors.toList());
        return childrenList;
    }
}

