package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.SysUser;
import com.mengze.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

//用户信息
@Service
public class UserDetailServiceImpl implements UserDetailsService
{

    @Autowired
    private SysUserMapper sysUserMapper;

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
        return new LoginUser(sysUser);
    }
}
