package com.mengze.service;

/**
 * @author mengze
 */
public interface SensitiveUtilService {

    /**
     * 内容校验敏感词
     * @param content 内容
     * @param email 评论者邮箱（用于判断是否为博主）
     * @return 是否敏感
     */
    Boolean checkSensitiveWords(String content, String email);
}
