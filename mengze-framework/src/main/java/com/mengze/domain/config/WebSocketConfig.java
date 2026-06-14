package com.mengze.domain.config;

import com.mengze.constants.WebSocketConstants;
import com.mengze.interceptor.WebSocketAuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 配置类
 *
 * 说明：
 * 1. 前台与后台分别使用不同的 WebSocket 端点，便于按来源隔离连接。
 * 2. 在握手阶段将端点类型写入会话属性，供后续鉴权拦截器使用。
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer
{
    @Autowired
    private WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry)
    {
        // 注册博客前台 WebSocket 端点，前台页面通过该地址建立连接
        registry.addEndpoint("/ws/blog")
                .addInterceptors(endpointTypeHandshakeInterceptor(WebSocketConstants.ENDPOINT_TYPE_BLOG))
                .setAllowedOriginPatterns("*");

        // 注册管理后台 WebSocket 端点，后台页面通过该地址建立连接
        registry.addEndpoint("/ws/admin")
                .addInterceptors(endpointTypeHandshakeInterceptor(WebSocketConstants.ENDPOINT_TYPE_ADMIN))
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry)
    {
        // 客户端订阅消息的前缀，例如 /topic/**、/queue/**
        registry.enableSimpleBroker("/topic", "/queue");
        // 客户端发送消息到服务端的前缀，例如 /app/**
        registry.setApplicationDestinationPrefixes("/app");
        // 点对点消息前缀，例如 /user/**
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration)
    {
        // 拦截 STOMP CONNECT 请求，统一处理 token、游客身份以及端点权限校验
        registration.interceptors(webSocketAuthInterceptor);
    }

    //握手拦截器，用于在 WebSocket 握手阶段写入端点类型。
    private HandshakeInterceptor endpointTypeHandshakeInterceptor(String endpointType)
    {
        return new HandshakeInterceptor()
        {
            @Override
            public boolean beforeHandshake(org.springframework.http.server.ServerHttpRequest request,
                                           org.springframework.http.server.ServerHttpResponse response,
                                           org.springframework.web.socket.WebSocketHandler wsHandler,
                                           Map<String, Object> attributes)
            {
                // 将当前端点类型写入会话属性，供后续鉴权时判断连接来源
                attributes.put(WebSocketConstants.ATTR_ENDPOINT_TYPE, endpointType);
                return true;
            }

            @Override
            public void afterHandshake(org.springframework.http.server.ServerHttpRequest request,
                                          org.springframework.http.server.ServerHttpResponse response,
                                          org.springframework.web.socket.WebSocketHandler wsHandler,
                                          Exception exception)
            {
                // 这里无需额外处理，握手完成后端点类型已保存在 attributes 中
            }
        };
    }
}
