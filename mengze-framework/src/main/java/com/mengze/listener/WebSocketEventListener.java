package com.mengze.listener;

import com.mengze.interceptor.WebSocketAuthInterceptor.WebSocketPrincipal;
import com.mengze.service.OnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;

//WebSocket连接事件监听器
@Component
public class WebSocketEventListener
{
    private static final String ADMIN_ONLINE_COUNT_TOPIC = "/topic/admin/online-count";
    private static final String BLOG_ONLINE_COUNT_TOPIC = "/topic/blog/online-count";

    @Autowired
    private OnlineUserService onlineUserService;

    //STOMP连接成功后记录在线用户并更新在线人数
    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();
        if (principal instanceof WebSocketPrincipal webSocketPrincipal){
            onlineUserService.online(sessionId, webSocketPrincipal.getName(), webSocketPrincipal.getLoginType());
        }
    }

    //有人订阅在线人数主题后立即推送当前在线人数
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if (ADMIN_ONLINE_COUNT_TOPIC.equals(destination)){
            onlineUserService.pushOnlineCount(WebSocketPrincipal.LOGIN_TYPE_ADMIN);
        }
        if (BLOG_ONLINE_COUNT_TOPIC.equals(destination)){
            onlineUserService.pushOnlineCount(WebSocketPrincipal.LOGIN_TYPE_BLOG);
        }
    }

    //STOMP断开连接后移除在线用户并更新在线人数
    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event)
    {
        onlineUserService.offline(event.getSessionId());
    }
}
