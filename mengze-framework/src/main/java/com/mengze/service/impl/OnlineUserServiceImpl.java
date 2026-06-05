package com.mengze.service.impl;

import com.mengze.constants.RedisKeyConstants;
import com.mengze.domain.vo.OnlineCountVo;
import com.mengze.service.OnlineUserService;
import com.mengze.utils.RedisCache;
import com.mengze.constants.WebSocketConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * WebSocket在线用户服务实现
 */
@Service
public class OnlineUserServiceImpl implements OnlineUserService
{
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    //标记用户为上线
    //建立 WebSocket 连接后记录 sessionId 与 userId 的关系
    //然后维护用户的会话集合和分类在线用户集合，最后更新最新在线人数
    @Override
    public void online(String sessionId, String userId, String loginType)
    {
        //会话 ID 或用户 ID 为空时不处理，避免写入无效在线状态
        if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(userId) || !StringUtils.hasText(loginType)){
            return;
        }

        String normalizedLoginType = normalizeLoginType(loginType);
        String userSessionsKey = getUserSessionsKey(userId, normalizedLoginType);
        String onlineUsersKey = getOnlineUsersKey(normalizedLoginType);

        //将用户加入 redis对应在线用户集合（去重后）
        redisCache.setCacheMapValue(RedisKeyConstants.WS_ONLINE_SESSIONS_KEY, sessionId, buildSessionValue(userId, normalizedLoginType));
        redisCache.addToSet(userSessionsKey, sessionId);
        redisCache.addToSet(onlineUsersKey, userId);
        pushOnlineCount(normalizedLoginType);
    }

    //标记用户为离线
    //WebSocket 连接断开后，根据 sessionId 删除会话映射
    //如果该用户已经没有其他活跃会话则将其从在线用户集合中移除。
    @Override
    public void offline(String sessionId)
    {
        //会话 ID 为空时不处理。
        if (!StringUtils.hasText(sessionId)){
            return;
        }

        //通过 sessionId 查 userId 与 loginType，用于清理该用户的会话集合
        String sessionValue = redisCache.getCacheMapValue(RedisKeyConstants.WS_ONLINE_SESSIONS_KEY, sessionId);

        //删除全局 sessionId -> userId/loginType 映射
        redisCache.delCacheMapValue(RedisKeyConstants.WS_ONLINE_SESSIONS_KEY, sessionId);
        if (StringUtils.hasText(sessionValue)){
            String[] sessionParts = sessionValue.split(":", 2);
            if (sessionParts.length == 2 && StringUtils.hasText(sessionParts[0]) && StringUtils.hasText(sessionParts[1])){
                String userId = sessionParts[0];
                String loginType = sessionParts[1];
                String userSessionsKey = getUserSessionsKey(userId, loginType);
                String onlineUsersKey = getOnlineUsersKey(loginType);

                //从用户会话集合中移除当前断开的 sessionId
                redisCache.removeFromSet(userSessionsKey, sessionId);
                long sessionCount = redisCache.getSetSize(userSessionsKey);

                //如果该用户没有任何剩余会话，则认为用户真正离线。
                if (sessionCount == 0){
                    redisCache.deleteObject(userSessionsKey);
                    redisCache.removeFromSet(onlineUsersKey, userId);
                }

                //离线状态变化后更新最新在线人数
                pushOnlineCount(loginType);
            }
        }
    }

    //获取当前在线人数（默认返回后台在线人数）
    @Override
    public OnlineCountVo getOnlineCount()
    {
        return getOnlineCount(WebSocketConstants.LOGIN_TYPE_ADMIN);
    }

    @Override
    public OnlineCountVo getOnlineCount(String loginType)
    {
        Set<String> onlineUsers = redisCache.getCacheSet(getOnlineUsersKey(normalizeLoginType(loginType)));
        long count = onlineUsers == null ? 0L : onlineUsers.size();
        return new OnlineCountVo(count, System.currentTimeMillis());
    }

    //更新当前在线人数（默认推送后台在线人数）
    @Override
    public void pushOnlineCount()
    {
        pushOnlineCount(WebSocketConstants.LOGIN_TYPE_ADMIN);
    }

    @Override
    public void pushOnlineCount(String loginType)
    {
        String normalizedLoginType = normalizeLoginType(loginType);
        String topic = getOnlineCountTopic(normalizedLoginType);
        messagingTemplate.convertAndSend(topic, getOnlineCount(normalizedLoginType));
    }

    //拼接指定用户的 WebSocket 会话集合 Key
    private String getUserSessionsKey(String userId, String loginType)
    {
        return RedisKeyConstants.WS_ONLINE_USER_SESSIONS_KEY_PREFIX + loginType + ":" + userId;
    }

    private String getOnlineUsersKey(String loginType)
    {
        return RedisKeyConstants.WS_ONLINE_USERS_KEY_PREFIX + loginType;
    }

    //获取在线数 topic
    private String getOnlineCountTopic(String loginType)
    {
        if (WebSocketConstants.LOGIN_TYPE_VISITOR.equals(loginType)){
            return WebSocketConstants.TOPIC_VISITOR_ONLINE_COUNT;
        }
        return WebSocketConstants.LOGIN_TYPE_BLOG.equals(loginType) ? WebSocketConstants.TOPIC_BLOG_ONLINE_COUNT : WebSocketConstants.TOPIC_ADMIN_ONLINE_COUNT;
    }

    private String buildSessionValue(String userId, String loginType)
    {
        return userId + ":" + loginType;
    }

    //登录用户类型
    private String normalizeLoginType(String loginType)
    {
        if (!StringUtils.hasText(loginType)){
            return WebSocketConstants.DEFAULT_LOGIN_TYPE;
        }
        return loginType;
    }
}
