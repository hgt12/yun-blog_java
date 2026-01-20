package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.SendCodeDto;
import com.mengze.domain.entity.User;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.UserMapper;
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

//认证服务类
@Service
public class AuthServiceImpl implements AuthService
{
    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private AliyunSmsServiceImpl aliyunSmsService;

    @Autowired
    UserMapper userMapper;

    @Autowired
    private RedisCache redisCache;

    //发送邮箱登录验证码
    @Override
    public ResponseResult sendEmailLoginCode(SendCodeDto dto) {
        return sendVerificationCode(dto.getEmail(), "email", "login", true);
    }

    //发送手机登录验证码
    @Override
    public ResponseResult sendPhoneLoginCode(SendCodeDto dto) {
        return sendVerificationCode(dto.getPhone(), "phone", "login", true);
    }

    //发送邮箱注册验证码
    @Override
    public ResponseResult sendEmailRegisterCode(SendCodeDto dto) {
        return sendVerificationCode(dto.getEmail(), "email", "register", false);
    }

    //发送手机号注册验证码
    @Override
    public ResponseResult sendPhoneRegisterCode(SendCodeDto dto) {
        return sendVerificationCode(dto.getPhone(), "phone", "register", false);
    }

    //发送邮箱忘记密码的验证码
    @Override
    public ResponseResult sendEmailForgotPasswordCode(SendCodeDto dto) {
        return sendVerificationCode(dto.getEmail(), "email", "forgot", true);
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

        //检查账号存在性
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
        String sendTimeKey = String.format("%s:send:time:%s:%s", scene, type, account);
        String lastSendTime = redisCache.getCacheObject(sendTimeKey);
        if (StringUtils.hasText(lastSendTime)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SMS_SEND_FREQUENTLY, "验证码发送过于频繁，请1分钟后再试");
        }

        //生成验证码并存入Redis
        String code = generateCode();
        String redisKey = String.format("%s:code:%s:%s", scene, type, account);
        redisCache.setCacheObject(redisKey, code, 5, TimeUnit.MINUTES);

        //记录发送时间
        redisCache.setCacheObject(sendTimeKey, String.valueOf(System.currentTimeMillis()), 1, TimeUnit.MINUTES);

        //记录IP发送次数
        recordIpSendCount(clientIp, type);

        // 发送验证码
        try {
            if ("email".equals(type)) {
                //三种情况
                switch (scene) {
                    case "login":
                        emailService.sendVerificationCodeByLogin(account, code);
                        break;
                    case "register":
                        emailService.sendVerificationCodeByRegister(account, code);
                        break;
                    case "forgot":
                        emailService.sendVerificationCodeByForgotPassword(account, code);
                        break;
                    default:
                        emailService.sendVerificationCodeByLogin(account, code);
                }
            } else {
                aliyunSmsService.sendVerificationCode(account, code);
            }
            System.out.println(String.format("发送%s验证码到%s: %s, 验证码: %s, IP: %s", 
                scene, type.equals("email") ? "邮箱" : "手机", account, code, clientIp));
        } catch (Exception e) {
            System.err.println(String.format("%s发送失败: %s", type.equals("email") ? "邮件" : "短信", e.getMessage()));
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
            if ("email".equals(type)) {
                return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_NOT_NULL);
            } else {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "手机号不能为空");
            }
        }

        // 验证手机号格式
        if ("phone".equals(type) && !account.matches("^1[3-9]\\d{9}$")) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_FORMAT_ERROR);
        }

        return null;
    }

    //根据场景检查账号
    private ResponseResult checkAccountForScene(String account, String type, String scene, boolean needAccountExists)
    {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        if ("email".equals(type)) {
            queryWrapper.eq(User::getEmail, account);
        } else {
            queryWrapper.eq(User::getPhone, account);
        }
        
        User user = userMapper.selectOne(queryWrapper);
        String accountTypeName = type.equals("email") ? "邮箱" : "手机号";
        
        if (needAccountExists) {
            // 登录和忘记密码场景：需要账号已存在
            if (user == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, 
                    String.format("该%s未注册", accountTypeName));
            }
        } else {
            // 注册场景：需要账号不存在
            if (user != null) {
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
        return "unknown";
    }

    //检查IP发送频率限制
    private ResponseResult checkIpLimit(String ip, String type)
    {
        // 检查1分钟内的发送次数（最多5次）
        String minuteKey = "login:ip:limit:minute:" + type + ":" + ip;
        Integer minuteCount = redisCache.getCacheObject(minuteKey);
        if (minuteCount != null && minuteCount >= 5) {
            return ResponseResult.errorResult(AppHttpCodeEnum.IP_REQUEST_LIMIT, "您的操作过于频繁，请1分钟后再试");
        }

        // 检查1小时内的发送次数（最多20次）
        String hourKey = "login:ip:limit:hour:" + type + ":" + ip;
        Integer hourCount = redisCache.getCacheObject(hourKey);
        if (hourCount != null && hourCount >= 20) {
            return ResponseResult.errorResult(AppHttpCodeEnum.IP_REQUEST_LIMIT, "您今日发送次数过多，请1小时后再试");
        }

        // 检查1天内的发送次数（最多50次，防止恶意攻击）
        String dayKey = "login:ip:limit:day:" + type + ":" + ip;
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
        String minuteKey = "login:ip:limit:minute:" + type + ":" + ip;
        Integer minuteCount = redisCache.getCacheObject(minuteKey);
        redisCache.setCacheObject(minuteKey, minuteCount == null ? 1 : minuteCount + 1, 1, TimeUnit.MINUTES);

        // 记录1小时内的发送次数
        String hourKey = "login:ip:limit:hour:" + type + ":" + ip;
        Integer hourCount = redisCache.getCacheObject(hourKey);
        redisCache.setCacheObject(hourKey, hourCount == null ? 1 : hourCount + 1, 1, TimeUnit.HOURS);

        // 记录1天内的发送次数
        String dayKey = "login:ip:limit:day:" + type + ":" + ip;
        Integer dayCount = redisCache.getCacheObject(dayKey);
        redisCache.setCacheObject(dayKey, dayCount == null ? 1 : dayCount + 1, 1, TimeUnit.DAYS);

        // 如果24小时内发送次数超过30次，记录异常日志
        if (dayCount != null && dayCount >= 30) {
            System.err.println("警告：检测到可能的异常发送行为 - IP: " + ip + ", 类型: " + type + ", 24小时内发送次数: " + (dayCount + 1));
        }
    }
}
