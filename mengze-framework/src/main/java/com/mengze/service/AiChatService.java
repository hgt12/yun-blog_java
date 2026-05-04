package com.mengze.service;

import com.mengze.enums.DetectionStatusEnum;

/**
 * AI聊天服务接口
 * @author mengze
 */
public interface AiChatService {
    /**
     * AI检测敏感词
     * @param word 待检测内容
     * @return DetectionStatusEnum 检测状态
     */
    DetectionStatusEnum aiCheckWord(String word);
}

