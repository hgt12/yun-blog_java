package com.mengze.interceptor;

import com.mengze.domain.entity.LoginUser;
import com.mengze.utils.JwtUtil;
import com.mengze.utils.RedisCache;
import io.jsonwebtoken.Claims;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Objects;

//WebSocket连接鉴权拦截器
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor
{
    private static final String ADMIN_LOGIN_KEY_PREFIX = "adminLogin:";
    private static final String BLOG_LOGIN_KEY_PREFIX = "blogLogin:";

    @Autowired
    private RedisCache redisCache;

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel)
    {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (Objects.isNull(accessor) || !StompCommand.CONNECT.equals(accessor.getCommand())){
            return message;
        }

        //获取STOMP CONNECT Header中的token
        String token = accessor.getFirstNativeHeader("token");
        if (!StringUtils.hasText(token)){
            throw new IllegalArgumentException("token不能为空");
        }

        Claims claims;
        try {
            claims = JwtUtil.parseJWT(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("token无效或过期！");
        }

        String userId = claims.getSubject();
        LoginUser loginUser = redisCache.getCacheObject(ADMIN_LOGIN_KEY_PREFIX + userId);
        String loginType = WebSocketPrincipal.LOGIN_TYPE_ADMIN;
        if (Objects.isNull(loginUser)){
            loginUser = redisCache.getCacheObject(BLOG_LOGIN_KEY_PREFIX + userId);
            loginType = WebSocketPrincipal.LOGIN_TYPE_BLOG;
        }
        if (Objects.isNull(loginUser)){
            throw new IllegalArgumentException("token无效或过期！");
        }

        //绑定当前登录用户身份，后续可通过Principal获取当前用户
        WebSocketPrincipal principal = new WebSocketPrincipal(userId, loginUser, loginType);
        accessor.setUser(principal);
        accessor.setHeader("loginUser", loginUser);
        accessor.setHeader("authentication", new UsernamePasswordAuthenticationToken(loginUser, null, null));
        return message;
    }

    //WebSocket用户身份信息
    public static class WebSocketPrincipal implements Principal
    {
        public static final String LOGIN_TYPE_ADMIN = "admin";
        public static final String LOGIN_TYPE_BLOG = "blog";

        private final String name;

        @Getter
        private final LoginUser loginUser;

        @Getter
        private final String loginType;

        public WebSocketPrincipal(String name, LoginUser loginUser, String loginType)
        {
            this.name = name;
            this.loginUser = loginUser;
            this.loginType = loginType;
        }

        @Override
        public String getName()
        {
            return name;
        }

    }
}
