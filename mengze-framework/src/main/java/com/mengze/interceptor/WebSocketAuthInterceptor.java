package com.mengze.interceptor;

import com.mengze.constants.RedisKeyConstants;
import com.mengze.domain.entity.LoginUser;
import com.mengze.utils.JwtUtil;
import com.mengze.utils.RedisCache;
import com.mengze.constants.WebSocketConstants;
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
import java.util.UUID;

/**
 * WebSocket 连接鉴权拦截器。
 *
 * 作用：
 * 1. 在 STOMP CONNECT 阶段校验 token。
 * 2. 根据握手阶段写入的端点类型，限制前台/后台用户只能连接对应端点。
 * 3. 如果没有 token，则自动按游客身份处理。
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor
{
    @Autowired
    private RedisCache redisCache;

    @Override
    public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel)
    {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (Objects.isNull(accessor) || !StompCommand.CONNECT.equals(accessor.getCommand())){
            return message;
        }

        // 从握手阶段保存的会话属性中读取端点类型，用于区分前台和后台连接
        String endpointType = (String) accessor.getSessionAttributes().get(WebSocketConstants.ATTR_ENDPOINT_TYPE);
        if (WebSocketConstants.ENDPOINT_TYPE_ADMIN.equals(endpointType)){
            accessor.setHeader(WebSocketConstants.HEADER_LOGIN_TYPE, WebSocketConstants.LOGIN_TYPE_ADMIN);
        } else if (WebSocketConstants.ENDPOINT_TYPE_BLOG.equals(endpointType)){
            accessor.setHeader(WebSocketConstants.HEADER_LOGIN_TYPE, WebSocketConstants.LOGIN_TYPE_BLOG);
        }

        // 获取 STOMP CONNECT Header 中携带的 token
        String token = accessor.getFirstNativeHeader(WebSocketConstants.HEADER_TOKEN);
        if (!StringUtils.hasText(token)){
            // 没有 token 时，按游客处理；如果前端传了 visitorId，则沿用，否则随机生成一个
            String visitorId = accessor.getFirstNativeHeader(WebSocketConstants.HEADER_VISITOR_ID);
            if (!StringUtils.hasText(visitorId)){
                visitorId = UUID.randomUUID().toString().replace("-", "");
            }
            VisitorPrincipal principal = new VisitorPrincipal(visitorId);
            accessor.setUser(principal);
            accessor.setHeader(WebSocketConstants.HEADER_LOGIN_TYPE, WebSocketConstants.LOGIN_TYPE_VISITOR);
            accessor.setHeader(WebSocketConstants.HEADER_VISITOR_ID, visitorId);
            return message;
        }

        Claims claims;
        try {
            claims = JwtUtil.parseJWT(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("token无效或过期！");
        }

        String userId = claims.getSubject();
        LoginUser loginUser;
        String loginType;

        // 根据端点类型只查询对应的登录缓存，避免前台/后台用户 ID 串号导致冲突
        if (WebSocketConstants.ENDPOINT_TYPE_ADMIN.equals(endpointType)){
            loginUser = redisCache.getCacheObject(RedisKeyConstants.ADMIN_LOGIN_KEY_PREFIX + userId);
            loginType = WebSocketConstants.LOGIN_TYPE_ADMIN;
        } else if (WebSocketConstants.ENDPOINT_TYPE_BLOG.equals(endpointType)){
            loginUser = redisCache.getCacheObject(RedisKeyConstants.BLOG_LOGIN_KEY_PREFIX + userId);
            loginType = WebSocketConstants.LOGIN_TYPE_BLOG;
        } else {
            throw new IllegalArgumentException("未知的 WebSocket 端点类型！");
        }

        if (Objects.isNull(loginUser)){
            throw new IllegalArgumentException("token无效或过期！");
        }

        // 绑定当前登录用户身份，后续可通过 Principal 获取当前用户
        WebSocketPrincipal principal = new WebSocketPrincipal(userId, loginUser, loginType);
        accessor.setUser(principal);
        accessor.setHeader(WebSocketConstants.HEADER_LOGIN_USER, loginUser);
        accessor.setHeader(WebSocketConstants.HEADER_LOGIN_TYPE, loginType);
        accessor.setHeader(WebSocketConstants.HEADER_AUTHENTICATION, new UsernamePasswordAuthenticationToken(loginUser, null, null));
        return message;
    }

    //WebSocket用户身份信息
    public static class WebSocketPrincipal implements Principal
    {
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

    //游客WebSocket身份信息
    public static class VisitorPrincipal implements Principal
    {
        private final String name;

        public VisitorPrincipal(String name)
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return name;
        }
    }
}
