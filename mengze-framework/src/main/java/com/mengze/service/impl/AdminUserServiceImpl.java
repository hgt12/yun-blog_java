package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.SysUser;
import com.mengze.domain.vo.AdminUserInfoVo;
import com.mengze.domain.vo.SysUserInfoVo;
import com.mengze.mapper.SysUserMapper;
import com.mengze.service.AdminUserService;
import com.mengze.service.SysMenuService;
import com.mengze.service.SysRoleService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户表(SysUser)表服务实现类(后台)
 *
 * @author makejava
 * @since 2026-01-14 15:21:53
 */
@Service("adminUserService")
public class AdminUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements AdminUserService
{
    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysRoleService sysRoleService;

    @Override
    public ResponseResult getAdminInfo()
    {
        //获取当前用户id
        LoginUser loginUser = SecurityUtils.getLoginUser();

        //根据用户id查询用户权限信息
        List<String> perms = sysMenuService.selectPermByUserId(loginUser.getSysUser().getId());
        //根据用户id查询用户角色信息
        List<String> roleCode = sysRoleService.selectRoleCodeByUserId(loginUser.getSysUser().getId());

        //获取用户信息
        SysUser sysUser = loginUser.getSysUser();
        SysUserInfoVo sysUserInfoVo = BeanCopyUtils.copyBean(sysUser, SysUserInfoVo.class);

        AdminUserInfoVo adminUserInfoVo = new AdminUserInfoVo(sysUserInfoVo, roleCode, perms);

        return ResponseResult.okResult(adminUserInfoVo);
    }
}

