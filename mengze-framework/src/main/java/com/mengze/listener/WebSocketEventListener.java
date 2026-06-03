package com.mengze.listener;

import com.mengze.service.OnlineUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.security.Principal;
import java.util.Objects;

//WebSocket连接事件监听器
@Component
public class WebSocketEventListener
{
    private static final String ONLINE_COUNT_TOPIC = "/topic/online-count";

    @Autowired
    private OnlineUserService onlineUserService;

    //STOMP连接成功后记录在线用户并更新在线人数
    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Principal principal = accessor.getUser();
        if (Objects.nonNull(principal)){
            onlineUserService.online(sessionId, principal.getName());
        }
    }

    //前端有人访问页面后立即给当前会话更新一次当前在线人数
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if (ONLINE_COUNT_TOPIC.equals(destination)){
            onlineUserService.pushOnlineCount();
        }
    }

    //STOMP断开连接后移除在线用户并更新在线人数
    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event)
    {
        onlineUserService.offline(event.getSessionId());
    }
}
