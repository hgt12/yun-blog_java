package com.mengze.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddMenuDto;
import com.mengze.domain.dto.MenuListDto;
import com.mengze.domain.dto.UpdateMenuDto;
import com.mengze.domain.vo.*;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysMenuMapper;
import com.mengze.domain.entity.SysMenu;
import com.mengze.service.SysMenuService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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

    //获取菜单下拉数据
    @Override
    public ResponseResult getMenuOptions(Boolean onlyParent)
    {
        //查询所有菜单选项
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();

        //查询可见的菜单
        queryWrapper.eq(SysMenu::getVisible, SystemConstants.VISIBLE);

        //如果前端的 onlyParent 为 true，只查询目录和菜单类型（不包含按钮）
        if (onlyParent != null && onlyParent) {
            queryWrapper.in(SysMenu::getType, SystemConstants.MENU_TYPE_C, SystemConstants.MENU_TYPE_M);
        }

        //按排序字段升序排列
        queryWrapper.orderByAsc(SysMenu::getSort);
        
        //查询所有符合条件的菜单
        List<SysMenu> menuList = list(queryWrapper);

        //转换为 MenuOptionVo 列表
        List<MenuOptionVo> menuOptionVos = BeanCopyUtils.copyBeanList(menuList, MenuOptionVo.class);

        //构建菜单树
        List<MenuOptionVo> menuTree = builderMenuOptionTree(menuOptionVos, 0L);

        return ResponseResult.okResult(menuTree);
    }

    //获取菜单表单详情
    @Override
    public ResponseResult<MenuFormDetailVo> getMenuFormDetail(Long id)
    {
        //根据菜单id查询表单数据
        SysMenu menu = getById(id);
        if (menu == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "菜单不存在！");
        }

        //转换成Vo
        MenuFormDetailVo menuFormDetailVo = BeanCopyUtils.copyBean(menu, MenuFormDetailVo.class);

        //转换 params 字段：JSON String -> List<MenuParamVo>
        if (StringUtils.hasText(menu.getParams())) {
            try {
                List<MenuParamVo> paramList = JSON.parseObject(
                    menu.getParams(),
                        new TypeReference<>() {}
                );
                menuFormDetailVo.setParams(paramList);
            } catch (Exception e) {
                //如果解析失败，设置为空列表
                menuFormDetailVo.setParams(new ArrayList<>());
            }
        } else {
            menuFormDetailVo.setParams(new ArrayList<>());
        }

        return ResponseResult.okResult(menuFormDetailVo);
    }

    //新增菜单
    @Override
    @Transactional
    public ResponseResult addMenu(AddMenuDto addMenuDto)
    {
        //校验父菜单是否存在（如果不是顶级菜单）
        if (addMenuDto.getParentId() != 0L) {
            SysMenu parentMenu = getById(addMenuDto.getParentId());
            if (parentMenu == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "父菜单不存在！");
            }
        }

        //校验路由路径：目录和菜单必填，按钮可不填
        if ((SystemConstants.MENU_TYPE_C.equals(addMenuDto.getType()) || 
             SystemConstants.MENU_TYPE_M.equals(addMenuDto.getType())) && 
            !StringUtils.hasText(addMenuDto.getRoutePath())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "目录和菜单的路由路径不能为空！");
        }

        //检查同一父菜单下是否存在同名菜单
        LambdaQueryWrapper<SysMenu> nameQueryWrapper = new LambdaQueryWrapper<>();
        nameQueryWrapper.eq(SysMenu::getParentId, addMenuDto.getParentId())
                       .eq(SysMenu::getName, addMenuDto.getName());
        long nameCount = count(nameQueryWrapper);

        if (nameCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该父菜单下已存在同名菜单，请勿重复添加！");
        }

        //检查同一父菜单下路由路径是否重复（仅对目录和菜单类型）
        if (StringUtils.hasText(addMenuDto.getPath()) && 
            (SystemConstants.MENU_TYPE_C.equals(addMenuDto.getType()) || 
             SystemConstants.MENU_TYPE_M.equals(addMenuDto.getType()))) {
            LambdaQueryWrapper<SysMenu> pathQueryWrapper = new LambdaQueryWrapper<>();
            pathQueryWrapper.eq(SysMenu::getParentId, addMenuDto.getParentId())
                           .eq(SysMenu::getPath, addMenuDto.getPath());
            long pathCount = count(pathQueryWrapper);
            
            if (pathCount > 0) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该父菜单下已存在相同路由地址！");
            }
        }

        //复制基本属性
        SysMenu menu = BeanCopyUtils.copyBean(addMenuDto, SysMenu.class);
        
        //将routePath的值同时保存到path中
        if (StringUtils.hasText(addMenuDto.getRoutePath())) {
            menu.setPath(addMenuDto.getRoutePath());
        }
        
        //处理 params 字段：List<MenuParamDto> -> JSON String
        if (addMenuDto.getParams() != null && !addMenuDto.getParams().isEmpty()) {
            menu.setParams(JSON.toJSONString(addMenuDto.getParams()));
        }

        //保存菜单
        sysMenuService.save(menu);

        return ResponseResult.okResult();
    }

    //更新菜单
    @Override
    @Transactional
    public ResponseResult updateMenu(Long id, UpdateMenuDto updateMenuDto)
    {

        //检查菜单是否存在
        SysMenu oldMenu = sysMenuService.getById(id);
        if (oldMenu == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "菜单不存在！");
        }

        //校验父菜单是否存在
        if (updateMenuDto.getParentId() != 0L) {
            SysMenu parentMenu = getById(updateMenuDto.getParentId());
            if (parentMenu == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "父菜单不存在！");
            }
            if (updateMenuDto.getParentId().equals(id)) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "父菜单不能是自己！");
            }
        }

        //校验目录/菜单必须有路由路径
        if ((SystemConstants.MENU_TYPE_C.equals(updateMenuDto.getType()) ||
                SystemConstants.MENU_TYPE_M.equals(updateMenuDto.getType())) &&
                !StringUtils.hasText(updateMenuDto.getRoutePath())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "目录和菜单的路由路径不能为空！");
        }

        //校验同一父菜单下是否存在同名菜单（排除自己）
        LambdaQueryWrapper<SysMenu> nameQuery = new LambdaQueryWrapper<>();
        nameQuery.eq(SysMenu::getParentId, updateMenuDto.getParentId())
                .eq(SysMenu::getName, updateMenuDto.getName())
                .ne(SysMenu::getId, id);

        if (count(nameQuery) > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该父菜单下已存在同名菜单！");
        }

        //更新菜单信息
        SysMenu menu = BeanCopyUtils.copyBean(updateMenuDto, SysMenu.class);
        menu.setId(id);

        //将routePath的值同时保存到path中
        if (StringUtils.hasText(updateMenuDto.getRoutePath())) {
            menu.setPath(updateMenuDto.getRoutePath());
        }

        //处理 params 字段
        if (updateMenuDto.getParams() != null) {
            menu.setParams(JSON.toJSONString(updateMenuDto.getParams()));
        }

        //更新菜单
        sysMenuService.updateById(menu);

        return ResponseResult.okResult();
    }

    //删除菜单
    @Override
    @Transactional
    public ResponseResult deleteMenu(Long id)
    {
        // 参数校验
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的菜单");
        }
        
        //获取菜单并判断是否存在
        SysMenu menu = sysMenuService.getById(id);
        if (menu == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该菜单不存在！");
        }

        //检查是否存在子菜单
        LambdaQueryWrapper<SysMenu> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysMenu::getParentId, id);
        long childCount = sysMenuService.count(queryWrapper);
        
        if (childCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, 
                String.format("菜单【%s】下存在 %d 个子菜单，无法删除！请先删除子菜单。", menu.getName(), childCount));
        }

        //执行删除
        sysMenuService.removeById(id);

        return ResponseResult.okResult();
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


    //创建MenuOption的菜单树
    private List<MenuOptionVo> builderMenuOptionTree(List<MenuOptionVo> menuOptionVos, Long parentId)
    {
        List<MenuOptionVo> menuTree = menuOptionVos.stream()
                .filter(menuOptionVo -> menuOptionVo.getParentId().equals(parentId))
                .map(menuOptionVo -> menuOptionVo.setChildren(getChildrenByMenuOption(menuOptionVo, menuOptionVos)))
                .collect(Collectors.toList());
        return menuTree;
    }
    //获取传入参数的 子菜单（MenuOption）集合  
    private List<MenuOptionVo> getChildrenByMenuOption(MenuOptionVo menuOptionVo, List<MenuOptionVo> menuOptionVos)
    {
        List<MenuOptionVo> childrenList = menuOptionVos.stream()
                .filter(m -> m.getParentId().equals(menuOptionVo.getId()))
                .map(m -> m.setChildren(getChildrenByMenuOption(m, menuOptionVos)))
                .collect(Collectors.toList());
        return childrenList;
    }
}

