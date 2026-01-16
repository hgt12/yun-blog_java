package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.UserInfoVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.UserMapper;
import com.mengze.domain.entity.User;
import com.mengze.service.UserService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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

    //国内手机号正则：1开头，第二位3-9，后面9位数字
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    //更新用户信息
    @Override
    public ResponseResult updateUserInfo(User user)
    {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //昵称不能为空
        if (!StringUtils.hasText(user.getNickname())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "昵称不能为空");
        }

        //邮箱和电话号码不能同时为空
        if (!StringUtils.hasText(user.getEmail()) && !StringUtils.hasText(user.getPhone())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "邮箱和电话号码不能同时为空");
        }

        //校验手机号格式
        if (StringUtils.hasText(user.getPhone()) && !user.getPhone().matches(PHONE_REGEX)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请输入正确的手机号码");
        }

        //检查手机号是否被其他用户使用
        if (StringUtils.hasText(user.getPhone())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, user.getPhone())
                    .ne(User::getId, currentUserId);
            User existUser = getOne(queryWrapper);
            if (existUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "你用别人的电话号码干嘛？");
            }
        }

        //检查邮箱是否被其他用户使用
        if (StringUtils.hasText(user.getEmail())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, user.getEmail())
                    .ne(User::getId, currentUserId);
            User existUser = getOne(queryWrapper);
            if (existUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "你用别人的邮箱干嘛？");
            }
        }

        //使用LambdaUpdateWrapper条件更新
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, currentUserId)
                .set(User::getNickname, user.getNickname())
                .set(User::getSex, user.getSex())
                .set(User::getPhone, user.getPhone())
                .set(User::getEmail, user.getEmail())
                .set(User::getAvatar, user.getAvatar());

        boolean success = update(null, wrapper);
        if (!success) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "更新用户信息失败");
        }
        return ResponseResult.okResult();
    }
}

