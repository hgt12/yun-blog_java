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
    private static final String ONLINE_COUNT_TOPIC = "/topic/online-count";

    //在线用户集合 Key，集合成员为 userId。
    private static final String ONLINE_USERS_KEY = "ws:online:users";

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
    //然后维护用户的会话集合和全局在线用户集合，最后更新最新在线人数
    @Override
    public void online(String sessionId, String userId)
    {
        //会话 ID 或用户 ID 为空时不处理，避免写入无效在线状态
        if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(userId)){
            return;
        }

        //保存 sessionId -> userId 映射，方便离线时根据 sessionId 找到对应用户
        redisCache.setCacheMapValue(ONLINE_SESSIONS_KEY, sessionId, userId);

        //保存当前用户拥有的 sessionId（支持同一用户多端或多标签页同时在线）
        redisCache.addToSet(getUserSessionsKey(userId), sessionId);

        //将用户加入在线用户集合（去重后）
        redisCache.addToSet(ONLINE_USERS_KEY, userId);

        //在线状态变化后向前端更新最新在线人数
        pushOnlineCount();
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

        //通过 sessionId 查 userId，用于清理该用户的会话集合
        String userId = redisCache.getCacheMapValue(ONLINE_SESSIONS_KEY, sessionId);

        //删除全局 sessionId -> userId 映射
        redisCache.delCacheMapValue(ONLINE_SESSIONS_KEY, sessionId);
        if (StringUtils.hasText(userId)){
            String userSessionsKey = getUserSessionsKey(userId);

            //从用户会话集合中移除当前断开的 sessionId
            redisCache.removeFromSet(userSessionsKey, sessionId);
            long sessionCount = redisCache.getSetSize(userSessionsKey);

            //如果该用户没有任何剩余会话，则认为用户真正离线。
            if (sessionCount == 0){
                redisCache.deleteObject(userSessionsKey);
                redisCache.removeFromSet(ONLINE_USERS_KEY, userId);
            }
        }
        //离线状态变化后更新最新在线人数
        pushOnlineCount();
    }

    //获取当前在线人数
    @Override
    public OnlineCountVo getOnlineCount()
    {
        Set<String> onlineUsers = redisCache.getCacheSet(ONLINE_USERS_KEY);
        long count = onlineUsers == null ? 0L : onlineUsers.size();
        return new OnlineCountVo(count, System.currentTimeMillis());
    }

    //更新当前在线人数
    @Override
    public void pushOnlineCount()
    {
        messagingTemplate.convertAndSend(ONLINE_COUNT_TOPIC, getOnlineCount());
    }

    //拼接指定用户的 WebSocket 会话集合 Key
    private String getUserSessionsKey(String userId)
    {
        return USER_SESSIONS_KEY_PREFIX + userId;
    }
}
