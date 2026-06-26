package com.mengze.service.impl;

import com.mengze.constants.RedisKeyConstants;
import com.mengze.service.CaptchaVerificationService;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 图形验证码二次验证服务实现
 * 基于 Redis 存储二次验证 Token，Token 一次性使用（用后即焚），
 * 有效期 2 分钟，与 application.yml 中 captcha.secondary.expire 配置保持一致。
 *
 * @author hgt
 * @since 2026-06-25
 */
@Service
public class CaptchaVerificationServiceImpl implements CaptchaVerificationService
{

    @Autowired
    private RedisCache redisCache;

    @Override
    public String generateToken(String captchaId)
    {
        // 生成唯一 Token
        String token = UUID.randomUUID().toString().replace("-", "");

        // 存入 Redis，记录关联的验证码 ID 便于追溯
        String redisKey = RedisKeyConstants.SECONDARY_TOKEN_KEY_PREFIX + token;
        redisCache.setCacheObject(redisKey, captchaId, RedisKeyConstants.TOKEN_EXPIRE_MINUTES, TimeUnit.MINUTES);

        return token;
    }

    @Override
    public boolean verifyAndConsumeToken(String token)
    {
        // Token 不能为空
        if (!StringUtils.hasText(token)) {
            return false;
        }

        String redisKey = RedisKeyConstants.SECONDARY_TOKEN_KEY_PREFIX + token;
        String captchaId = redisCache.getCacheObject(redisKey);

        if (captchaId != null) {
            // Token 一次性使用，验证后立即删除（用后即焚）
            redisCache.deleteObject(redisKey);
            return true;
        }

        return false;
    }
}
