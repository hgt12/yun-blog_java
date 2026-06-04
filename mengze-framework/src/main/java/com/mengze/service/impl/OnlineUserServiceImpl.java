package com.mengze.service.impl;

import com.mengze.domain.vo.OnlineCountVo;
import com.mengze.service.OnlineUserService;
import com.mengze.utils.RedisCache;
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
    //后台系统
    private static final String ADMIN_ONLINE_COUNT_TOPIC = "/topic/admin/online-count";

    //前台系统
    private static final String BLOG_ONLINE_COUNT_TOPIC = "/topic/blog/online-count";

    //在线用户集合 Key，按登录类型分别统计。
    private static final String ONLINE_USERS_KEY_PREFIX = "ws:online:users:";

    //WebSocket 会话与用户的映射 Key,用于断开连接时反查用户
    private static final String ONLINE_SESSIONS_KEY = "ws:online:sessions";

    //单个用户对应的 WebSocket 会话集合 Key 前缀
    private static final String USER_SESSIONS_KEY_PREFIX = "ws:online:user:sessions:";

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

        String userSessionsKey = getUserSessionsKey(userId, loginType);
        String onlineUsersKey = getOnlineUsersKey(loginType);

        //保存 sessionId -> userId/loginType 映射，方便离线时根据 sessionId 找到对应用户
        redisCache.setCacheMapValue(ONLINE_SESSIONS_KEY, sessionId, buildSessionValue(userId, loginType));

        //保存当前用户拥有的 sessionId（支持同一用户多端或多标签页同时在线）
        redisCache.addToSet(userSessionsKey, sessionId);

        //将用户加入对应在线用户集合（去重后）
        redisCache.addToSet(onlineUsersKey, userId);

        //在线状态变化后向前端更新最新在线人数
        pushOnlineCount(loginType);
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
        String sessionValue = redisCache.getCacheMapValue(ONLINE_SESSIONS_KEY, sessionId);

        //删除全局 sessionId -> userId/loginType 映射
        redisCache.delCacheMapValue(ONLINE_SESSIONS_KEY, sessionId);
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
                return;
            }
        }
    }

    //获取当前在线人数（默认返回后台在线人数）
    @Override
    public OnlineCountVo getOnlineCount()
    {
        return getOnlineCount("admin");
    }

    @Override
    public OnlineCountVo getOnlineCount(String loginType)
    {
        Set<String> onlineUsers = redisCache.getCacheSet(getOnlineUsersKey(loginType));
        long count = onlineUsers == null ? 0L : onlineUsers.size();
        return new OnlineCountVo(count, System.currentTimeMillis());
    }

    //更新当前在线人数（默认推送后台在线人数）
    @Override
    public void pushOnlineCount()
    {
        pushOnlineCount("admin");
    }

    @Override
    public void pushOnlineCount(String loginType)
    {
        String topic = getOnlineCountTopic(loginType);
        messagingTemplate.convertAndSend(topic, getOnlineCount(loginType));
    }

    //拼接指定用户的 WebSocket 会话集合 Key
    private String getUserSessionsKey(String userId, String loginType)
    {
        return USER_SESSIONS_KEY_PREFIX + loginType + ":" + userId;
    }

    private String getOnlineUsersKey(String loginType)
    {
        return ONLINE_USERS_KEY_PREFIX + loginType;
    }

    private String getOnlineCountTopic(String loginType)
    {
        return "blog".equals(loginType) ? BLOG_ONLINE_COUNT_TOPIC : ADMIN_ONLINE_COUNT_TOPIC;
    }

    private String buildSessionValue(String userId, String loginType)
    {
        return userId + ":" + loginType;
    }
}
