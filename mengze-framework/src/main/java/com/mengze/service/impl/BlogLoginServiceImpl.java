package com.mengze.service.impl;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.User;
import com.mengze.domain.vo.BlogUserLoginVo;
import com.mengze.domain.vo.UserInfoVo;
import com.mengze.service.BlogLoginService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.GravatarUtils;
import com.mengze.utils.JwtUtil;
import com.mengze.utils.RedisCache;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

// 登录认证
@Service
public class BlogLoginServiceImpl implements BlogLoginService
{

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Override
    public ResponseResult login(User user)
    {

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        //判断是否认证通过
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("用户名或密码错误！");
        }
        //获取userid生成token
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userId = loginUser.getUser().getId().toString();
        String jwt = JwtUtil.createJWT(userId);//传数据进去jwt加密,最终生成一个字符串类型的jwt
        //把用户信息存入Redis (格式为 bloglogin：id)
        redisCache.setCacheObject("bloglogin:" + userId, loginUser);

        //把token和userInfo封装，返回
        //把User转化成UserInfoVo
        User user1 = loginUser.getUser();
        UserInfoVo userInfoVo = BeanCopyUtils.copyBean(user1, UserInfoVo.class);
        
        // 如果昵称为空，使用用户名代替
        if (!StringUtils.hasText(userInfoVo.getNickname())) {
            userInfoVo.setNickname(user1.getUsername());
        }
        // 如果头像为空，根据邮箱生成头像
        if (!StringUtils.hasText(userInfoVo.getAvatar())) {
            userInfoVo.setAvatar(GravatarUtils.getGravatarUrl(user1.getEmail()));
        }
        
        BlogUserLoginVo vo = new BlogUserLoginVo(jwt, userInfoVo);
        return ResponseResult.okResult(vo);
    }

    @Override
    public ResponseResult logout()
    {
        //获取token，解析获取userid(在哪获取token呢，去之前将token封装存储在SecurityContextHolder里面拿)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        //获取uesrid
        Long userId = loginUser.getUser().getId();
        //删除redis中的用户信息
        redisCache.deleteObject("bloglogin:" + userId);
        return ResponseResult.okResult();
    }
}
