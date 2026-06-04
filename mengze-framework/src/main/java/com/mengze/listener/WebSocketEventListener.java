package com.mengze.listener;

import com.mengze.constants.WebSocketConstants;
import com.mengze.interceptor.WebSocketAuthInterceptor.VisitorPrincipal;
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
            return;
        }
        if (principal instanceof VisitorPrincipal visitorPrincipal){
            onlineUserService.online(sessionId, visitorPrincipal.getName(), WebSocketConstants.LOGIN_TYPE_VISITOR);
        }
    }

    //有人在预览页面则将人数推送当前在线人数
    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event)
    {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String destination = accessor.getDestination();
        if (WebSocketConstants.TOPIC_ADMIN_ONLINE_COUNT.equals(destination)){
            onlineUserService.pushOnlineCount(WebSocketConstants.LOGIN_TYPE_ADMIN);
            return;
        }
        if (WebSocketConstants.TOPIC_BLOG_ONLINE_COUNT.equals(destination)){
            onlineUserService.pushOnlineCount(WebSocketConstants.LOGIN_TYPE_BLOG);
            return;
        }
        if (WebSocketConstants.TOPIC_VISITOR_ONLINE_COUNT.equals(destination)){
            onlineUserService.pushOnlineCount(WebSocketConstants.LOGIN_TYPE_VISITOR);
        }
    }

    //STOMP断开连接后移除在线用户并更新在线人数
    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event)
    {
        onlineUserService.offline(event.getSessionId());
    }
}
