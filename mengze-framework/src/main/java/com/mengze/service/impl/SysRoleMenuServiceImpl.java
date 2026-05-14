package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.mapper.SysRoleMenuMapper;
import com.mengze.domain.entity.SysRoleMenu;
import com.mengze.service.SysRoleMenuService;
import org.springframework.stereotype.Service;

/**
 * 角色菜单关联表(SysRoleMenu)表服务实现类
 *
 * @author makejava
 * @since 2026-05-14 21:18:47
 */
@Service("sysRoleMenuService")
public class SysRoleMenuServiceImpl extends ServiceImpl<SysRoleMenuMapper, SysRoleMenu> implements SysRoleMenuService {

}

