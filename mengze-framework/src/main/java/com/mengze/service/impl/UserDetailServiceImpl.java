package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.SysUser;
import com.mengze.mapper.SysUserMapper;
import com.mengze.mapper.SysMenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

//用户信息
@Service
public class UserDetailServiceImpl implements UserDetailsService
{

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private SysMenuMapper sysMenuMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        //根据用户名查询用户信息
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();// 查询条件
        queryWrapper.eq(SysUser::getUsername, username);
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
        //判断是否查到用户，如果没查到抛出异常
        if (Objects.isNull(sysUser)){
            throw new RuntimeException("用户不存在");
        }

        //将用户所拥有的权限添加进去
        LoginUser loginUser = new LoginUser(sysUser);
        List<String> permissions = sysMenuMapper.selectPermByUserId(sysUser.getId());
        if (permissions == null || permissions.isEmpty()) {
            permissions = Collections.emptyList();
        }
        loginUser.setPermissions(permissions);
        return loginUser;
    }
}
