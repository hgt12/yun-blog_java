package com.mengze.service;

import com.mengze.domain.vo.OnlineCountVo;

/**
 * WebSocket在线用户服务
 */
public interface OnlineUserService
{
    void online(String sessionId, String userId);

    void offline(String sessionId);

    OnlineCountVo getOnlineCount();

    void pushOnlineCount();
}
