package com.mengze.service.impl;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UserLoginDto;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.SysUser;
import com.mengze.domain.vo.AdminAuthVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import com.mengze.service.AdminAuthService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.CaptchaUtil;
import com.mengze.utils.JwtUtil;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

// 后台管理系统登录认证
@Service
public class AdminAuthServiceImpl implements AdminAuthService
{
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Override
    public ResponseResult login(UserLoginDto dto)
    {
        // 预防编程，用户名是否为空
        if (!StringUtils.hasText(dto.getUsername())) {
            // 提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }
        
        // 验证码校验（如果提供了验证码）
        if (StringUtils.hasText(dto.getCaptchaId()) && StringUtils.hasText(dto.getCaptchaCode()))
        {
            String cacheKey = "captcha:" + dto.getCaptchaId();
            String cachedCode = redisCache.getCacheObject(cacheKey);
            
            if (cachedCode == null) {
                throw new SystemException(AppHttpCodeEnum.CAPTCHA_EXPIRED);
            }
            
            if (!cachedCode.equalsIgnoreCase(dto.getCaptchaCode())) {
                throw new SystemException(AppHttpCodeEnum.CAPTCHA_ERROR);
            }
            
            // 验证成功后删除验证码
            redisCache.deleteObject(cacheKey);
        }
        
        // 将DTO转换为User实体
        SysUser sysUser = BeanCopyUtils.copyBean(dto, SysUser.class);

        UsernamePasswordAuthenticationToken authenticationToken = 
            new UsernamePasswordAuthenticationToken(sysUser.getUsername(), sysUser.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        
        //判断是否认证通过
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("用户名或密码错误！");
        }
        
        //获取userid生成token
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        SysUser sysUserInfo = loginUser.getSysUser();
        String userId = sysUserInfo.getId().toString();
        
        // 生成访问令牌
        String token = JwtUtil.createJWT(userId);
        
        // 生成刷新令牌（令牌有效时间为七天）
        String refreshToken = JwtUtil.createJWT(userId, 7 * 24 * 60 * 60 * 1000L);
        
        //把用户信息存入Redis (格式为 adminLogin:id，用于后台管理系统)
        redisCache.setCacheObject("adminLogin:" + userId, loginUser);
        
        // 构建响应对象
        AdminAuthVo vo = new AdminAuthVo(token, refreshToken, JwtUtil.JWT_TTL / 1000);
        return ResponseResult.okResult(vo);
    }

    //获取图形验证码
    @Override
    public ResponseResult captcha(UserLoginDto dto)
    {
        try {
            // 生成验证码ID
            String captchaId = UUID.randomUUID().toString();
            
            // 生成验证码文本
            String code = CaptchaUtil.generateCode();
            
            // 生成验证码图片Base64
            String base64Img = CaptchaUtil.generateImageBase64(code);
            
            // 将验证码存入Redis，有效期2分钟
            redisCache.setCacheObject("captcha:" + captchaId, code, 2, java.util.concurrent.TimeUnit.MINUTES);
            
            // 在控制台输出验证码（方便开发调试）
            System.out.println("========================================");
            System.out.println("验证码ID: " + captchaId);
            System.out.println("验证码内容: " + code);
            System.out.println("========================================");
            
            // 构建响应数据
            Map<String, String> result = new HashMap<>();
            result.put("captchaId", captchaId);
            result.put("captchaBase64", base64Img);
            
            return ResponseResult.okResult(result);
        } catch (Exception e) {
            throw new SystemException(AppHttpCodeEnum.SYSTEM_ERROR);
        }
    }
}
