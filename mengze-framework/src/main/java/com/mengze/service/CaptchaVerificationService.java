package com.mengze.service;

/**
 * 图形验证码二次验证服务接口
 * 一次验证（滑块/旋转等）通过后，后端颁发一个临时 Token，
 * 后续的敏感操作（如发送短信/邮箱验证码）需要携带此 Token 进行二次验证。
 *
 * @author hgt
 * @since 2026-06-25
 */
public interface CaptchaVerificationService {

    /**
     * 图形验证码一次校验通过后，生成二次验证 Token
     *
     * @param captchaId 验证码唯一标识
     * @return 二次验证 Token（前端需在后续敏感操作中携带此 Token）
     */
    String generateToken(String captchaId);

    /**
     * 校验并消费二次验证 Token（一次性使用，用后即焚）
     *
     * @param token 二次验证 Token
     * @return true-验证通过，false-验证失败（Token 不存在或已过期）
     */
    boolean verifyAndConsumeToken(String token);
}
