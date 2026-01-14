package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.UserInfoVo;
import com.mengze.mapper.UserMapper;
import com.mengze.domain.entity.User;
import com.mengze.service.UserService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.stereotype.Service;

/**
 * 用户表(User)表服务实现类
 *
 * @author makejava
 * @since 2026-01-14 15:21:53
 */
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService
{

    //查询用户信息
    @Override
    public ResponseResult userInfo()
    {
        //获取当前用户id
        Long userId = SecurityUtils.getUserId();
        //根据用户id查询用户信息
        User byId = getById(userId);
        //封装成UserInfo
        UserInfoVo vo = BeanCopyUtils.copyBean(byId, UserInfoVo.class);
        return ResponseResult.okResult(vo);
    }
}

