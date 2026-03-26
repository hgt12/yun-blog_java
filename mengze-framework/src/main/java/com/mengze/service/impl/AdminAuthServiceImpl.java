package com.mengze.service.impl;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UserLoginDto;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.User;
import com.mengze.domain.vo.AdminAuthVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import com.mengze.service.AdminAuthService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.JwtUtil;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

// 后台管理系统登录认证
@Service
public class AdminAuthServiceImpl implements AdminAuthService
{
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Override
    public ResponseResult login(UserLoginDto dto)
    {
        // 预防编程，用户名是否为空
        if (!StringUtils.hasText(dto.getUsername())) {
            // 提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }
        
        // 将DTO转换为User实体
        User user = BeanCopyUtils.copyBean(dto, User.class);

        UsernamePasswordAuthenticationToken authenticationToken = 
            new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        
        //判断是否认证通过
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("用户名或密码错误！");
        }
        
        //获取userid生成token
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        User userInfo = loginUser.getUser();
        String userId = userInfo.getId().toString();
        
        // 生成访问令牌
        String token = JwtUtil.createJWT(userId);
        
        // 生成刷新令牌（令牌有效时间为七天）
        String refreshToken = JwtUtil.createJWT(userId, 7 * 24 * 60 * 60 * 1000L);
        
        //把用户信息存入Redis (格式为 adminLogin:id，用于后台管理系统)
        redisCache.setCacheObject("adminLogin:" + userId, loginUser);
        
        // 构建响应对象
        AdminAuthVo vo = new AdminAuthVo(token, refreshToken, JwtUtil.JWT_TTL / 1000);
        return ResponseResult.okResult(vo);
    }
}
