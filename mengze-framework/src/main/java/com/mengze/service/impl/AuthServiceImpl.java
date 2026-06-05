package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mengze.constants.RedisKeyConstants;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.SendCodeDto;
import com.mengze.domain.entity.SysUser;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysUserMapper;
import com.mengze.service.AuthService;
import com.mengze.utils.IpUtils;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;
import java.util.concurrent.TimeUnit;

//前台发送验证码服务类
@Service
public class AuthServiceImpl implements AuthService
{
    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private AliyunSmsServiceImpl aliyunSmsService;

    @Autowired
    SysUserMapper sysUserMapper;

    @Autowired
    private RedisCache redisCache;

    //前台统一发送验证码接口
    @Override
    public ResponseResult sendCode(SendCodeDto dto)
    {
        //验证必填参数
        if (!StringUtils.hasText(dto.getAccount())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "账号不能为空");
        }
        if (!StringUtils.hasText(dto.getType())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码类型不能为空");
        }
        
        String account = dto.getAccount().trim();
        String type = dto.getType().toLowerCase();
        
        //验证类型的有效性
        if (!type.equals(SystemConstants.VERIFY_TYPE_LOGIN)
                && !type.equals(SystemConstants.VERIFY_TYPE_REGISTER)
                && !type.equals(SystemConstants.VERIFY_TYPE_FORGOT_PASSWORD)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码类型无效");
        }
        
        //判断登录类型（邮箱或手机号）
        String channel;
        if (account.contains("@")) {
            //包含@符号，判断为邮箱
            channel = SystemConstants.LOGIN_TYPE_EMAIL;
        } else if (account.matches("^1[3-9]\\d{9}$")) {
            //符合手机号格式
            channel = SystemConstants.LOGIN_TYPE_PHONE;
        } else {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请输入正确的邮箱或手机号");
        }
        
        //判断是否需要判断账号已存在（登录和忘记密码需要，注册不需要）
        boolean needAccountExists = !type.equals(SystemConstants.VERIFY_TYPE_REGISTER);
        
        return sendVerificationCode(account, channel, type, needAccountExists);
    }

    //通用验证码发送方法
    //account 账号（邮箱或手机号）
    //type 类型（email/phone）
    //scene 场景（login/register/forgot）
    //needAccountExists 是否需要验证账号已存在（登录和忘记密码需要，注册不需要）
    private ResponseResult sendVerificationCode(String account, String type, String scene, boolean needAccountExists)
    {
        //验证账号不能为空
        ResponseResult validateResult = validateAccount(account, type);
        if (validateResult != null) {
            return validateResult;
        }

        //检查账号是否存在
        ResponseResult accountCheckResult = checkAccountForScene(account, type, scene, needAccountExists);
        if (accountCheckResult != null) {
            return accountCheckResult;
        }

        //获取客户端IP并检查限流
        String clientIp = getClientIp();
        ResponseResult ipCheckResult = checkIpLimit(clientIp, type);
        if (ipCheckResult != null) {
            return ipCheckResult;
        }

        //检查账号发送频率限制
        String sendTimeKey = String.format(RedisKeyConstants.CODE_SEND_TIME_KEY_TEMPLATE, scene, type, account);
        String lastSendTime = redisCache.getCacheObject(sendTimeKey);
        if (StringUtils.hasText(lastSendTime)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SMS_SEND_FREQUENTLY, "验证码发送过于频繁，请1分钟后再试");
        }

        //生成验证码并存入Redis
        String code = generateCode();
        String redisKey = String.format(RedisKeyConstants.CODE_KEY_TEMPLATE, scene, type, account);
        redisCache.setCacheObject(redisKey, code, 5, TimeUnit.MINUTES);

        //记录发送时间
        redisCache.setCacheObject(sendTimeKey, String.valueOf(System.currentTimeMillis()), 1, TimeUnit.MINUTES);

        //记录IP发送次数
        recordIpSendCount(clientIp, type);

        // 发送验证码
        try {
            if (SystemConstants.CODE_TYPE_EMAIL.equals(type))
            {
                //三种情况
                switch (scene) {
                    case SystemConstants.VERIFY_TYPE_REGISTER:
                        emailService.sendVerificationCodeByRegister(account, code);
                        break;
                    case SystemConstants.VERIFY_TYPE_FORGOT_PASSWORD:
                        emailService.sendVerificationCodeByForgotPassword(account, code);
                        break;
                    case SystemConstants.VERIFY_TYPE_LOGIN:
                    default:
                        emailService.sendVerificationCodeByLogin(account, code);
                }
            } else {
                aliyunSmsService.sendVerificationCode(account, code);
            }
            System.out.println(String.format("发送%s验证码到%s: %s, 验证码: %s, IP: %s", 
                scene, type.equals(SystemConstants.CODE_TYPE_EMAIL) ? "邮箱" : "手机", account, code, clientIp));
        } catch (Exception e) {
            System.err.println(String.format("%s发送失败: %s", type.equals(SystemConstants.CODE_TYPE_EMAIL) ? "邮件" : "短信", e.getMessage()));
            // 发送失败时删除Redis记录
            redisCache.deleteObject(redisKey);
            redisCache.deleteObject(sendTimeKey);
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码发送失败，请稍后重试");
        }

        return ResponseResult.okResult("验证码已发送，5分钟内有效");
    }

    //验证账号格式
    private ResponseResult validateAccount(String account, String type)
    {
        if (!StringUtils.hasText(account)) {
            if (SystemConstants.LOGIN_TYPE_EMAIL.equals(type)) {
                return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_NOT_NULL);
            } else {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "手机号不能为空");
            }
        }

        // 验证手机号格式
        if (SystemConstants.LOGIN_TYPE_PHONE.equals(type) && !account.matches("^1[3-9]\\d{9}$")) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_FORMAT_ERROR);
        }

        // 验证手机号格式
        if (SystemConstants.LOGIN_TYPE_EMAIL.equals(type) && !account.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_FORMAT_ERROR);
        }

        return null;
    }

    //根据场景检查账号
    private ResponseResult checkAccountForScene(String account, String type, String scene, boolean needAccountExists)
    {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        if (SystemConstants.LOGIN_TYPE_EMAIL.equals(type)) {
            queryWrapper.eq(SysUser::getEmail, account);
        } else {
            queryWrapper.eq(SysUser::getPhone, account);
        }
        
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
        String accountTypeName = type.equals(SystemConstants.LOGIN_TYPE_EMAIL) ? "邮箱" : "手机号";
        
        if (needAccountExists) {
            // 登录和忘记密码场景：需要账号已存在
            if (sysUser == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, 
                    String.format("该%s未注册", accountTypeName));
            }
        } else {
            // 注册场景：需要账号不存在
            if (sysUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, 
                    String.format("该%s已被注册", accountTypeName));
            }
        }
        
        return null;
    }

    //生成6位随机验证码
    private String generateCode()
    {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    //获取客户端IP地址
    private String getClientIp()
    {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return IpUtils.getIpAddr(request);
            }
        } catch (Exception e) {
            System.err.println("获取客户端IP失败: " + e.getMessage());
        }
        return SystemConstants.UNKNOWN;
    }

    //检查IP发送频率限制
    private ResponseResult checkIpLimit(String ip, String type)
    {
        // 检查1分钟内的发送次数（最多5次）
        String minuteKey = RedisKeyConstants.AUTH_IP_LIMIT_MINUTE_KEY_PREFIX + type + ":" + ip;
        Integer minuteCount = redisCache.getCacheObject(minuteKey);
        if (minuteCount != null && minuteCount >= 5) {
            return ResponseResult.errorResult(AppHttpCodeEnum.IP_REQUEST_LIMIT, "您的操作过于频繁，请1分钟后再试");
        }

        // 检查1小时内的发送次数（最多20次）
        String hourKey = RedisKeyConstants.AUTH_IP_LIMIT_HOUR_KEY_PREFIX + type + ":" + ip;
        Integer hourCount = redisCache.getCacheObject(hourKey);
        if (hourCount != null && hourCount >= 20) {
            return ResponseResult.errorResult(AppHttpCodeEnum.IP_REQUEST_LIMIT, "您今日发送次数过多，请1小时后再试");
        }

        // 检查1天内的发送次数（最多50次，防止恶意攻击）
        String dayKey = RedisKeyConstants.AUTH_IP_LIMIT_DAY_KEY_PREFIX + type + ":" + ip;
        Integer dayCount = redisCache.getCacheObject(dayKey);
        if (dayCount != null && dayCount >= 50) {
            System.err.println("检测到异常发送行为 - IP: " + ip + ", 类型: " + type + ", 24小时内发送次数: " + dayCount);
            return ResponseResult.errorResult(AppHttpCodeEnum.IP_REQUEST_LIMIT, "系统检测到异常行为，请24小时后再试或联系管理员");
        }

        return null;
    }

    //记录IP发送次数
    private void recordIpSendCount(String ip, String type)
    {
        // 记录1分钟内的发送次数
        String minuteKey = RedisKeyConstants.AUTH_IP_LIMIT_MINUTE_KEY_PREFIX + type + ":" + ip;
        Integer minuteCount = redisCache.getCacheObject(minuteKey);
        redisCache.setCacheObject(minuteKey, minuteCount == null ? 1 : minuteCount + 1, 1, TimeUnit.MINUTES);

        // 记录1小时内的发送次数
        String hourKey = RedisKeyConstants.AUTH_IP_LIMIT_HOUR_KEY_PREFIX + type + ":" + ip;
        Integer hourCount = redisCache.getCacheObject(hourKey);
        redisCache.setCacheObject(hourKey, hourCount == null ? 1 : hourCount + 1, 1, TimeUnit.HOURS);

        // 记录1天内的发送次数
        String dayKey = RedisKeyConstants.AUTH_IP_LIMIT_DAY_KEY_PREFIX + type + ":" + ip;
        Integer dayCount = redisCache.getCacheObject(dayKey);
        redisCache.setCacheObject(dayKey, dayCount == null ? 1 : dayCount + 1, 1, TimeUnit.DAYS);

        // 如果24小时内发送次数超过30次，记录异常日志
        if (dayCount != null && dayCount >= 30) {
            System.err.println("警告：检测到可能的异常发送行为 - IP: " + ip + ", 类型: " + type + ", 24小时内发送次数: " + (dayCount + 1));
        }
    }
}
