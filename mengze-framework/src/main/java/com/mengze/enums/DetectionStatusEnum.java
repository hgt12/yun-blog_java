package com.mengze.enums;

/**
 * 检测状态枚举
 * @author mengze
 */
public enum DetectionStatusEnum {
    /**
     * 敏感内容
     */
    SENSITIVE,
    
    /**
     * 内容安全
     */
    CLEAR,
    
    /**
     * 建议云端检测
     */
    SUGGEST_CLOUD_CHECK;
    
    /**
     * 是否应该拦截
     * @return true-应该拦截，false-不拦截
     */
    public boolean shouldBlock() {
        return this == SENSITIVE;
    }
    
    /**
     * 是否需要云端检测
     * @return true-需要云端检测，false-不需要
     */
    public boolean needsCloudCheck() {
        return this == SUGGEST_CLOUD_CHECK;
    }
}
