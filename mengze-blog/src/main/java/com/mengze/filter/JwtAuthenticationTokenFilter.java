package com.mengze.filter;

import com.alibaba.fastjson.JSON;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.LoginUser;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.utils.JwtUtil;
import com.mengze.utils.RedisCache;
import com.mengze.utils.WebUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

//jwt认证过滤器
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter
{
    @Autowired
    private RedisCache redisCache;

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException
    {
        //获取请求头中的token
        String token = httpServletRequest.getHeader("token");
        if (!StringUtils.hasText(token)){
            //如果没有token，说明该接口不需要登录，直接放行
            filterChain.doFilter(httpServletRequest, httpServletResponse);
            return;
        }
        //解析获取userId
        Claims claims = null;
        try {
            claims = JwtUtil.parseJWT(token);
        } catch (Exception e) {
            e.printStackTrace();
            //token 超时，token 非法
            //响应告诉前端要重新登录
            ResponseResult responseResult = ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
            WebUtils.renderString(httpServletResponse, JSON.toJSONString(responseResult));//将传入的数据转化为json格式
            return;
        }
        String userId = claims.getSubject();
        //从redis中获取用户信息
        LoginUser loginUser = redisCache.getCacheObject("blogLogin:" + userId);
        //如果获取不到,提示重新登录
        if (Objects.isNull(loginUser)){
            ResponseResult responseResult = ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
            WebUtils.renderString(httpServletResponse, JSON.toJSONString(responseResult));//将传入的数据转化为json格式
            return;
        }

        //存入SecurityContextHolder
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(loginUser, null, null);//已认证过，传三个参数
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //放行
        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }
}
