package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LoginByEmailOrPhoneDto;
import com.mengze.domain.dto.LoginDto;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.SysUser;
import com.mengze.domain.vo.BlogUserLoginVo;
import com.mengze.domain.vo.SysUserInfoVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import com.mengze.mapper.SysUserMapper;
import com.mengze.mapper.SysRoleMapper;
import com.mengze.service.BlogLoginService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.GravatarUtils;
import com.mengze.utils.JwtUtil;
import com.mengze.utils.RedisCache;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

// 登录认证
@Service
public class BlogLoginServiceImpl implements BlogLoginService
{

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Override
    public ResponseResult login(LoginDto dto)
    {
        // 预防编程，用户名是否为空
        if (!StringUtils.hasText(dto.getUsername())) {
            // 提示 必须要传用户名
            throw new SystemException(AppHttpCodeEnum.REQUIRE_USERNAME);
        }
        
        // 将DTO转换为User实体
        SysUser sysUser = BeanCopyUtils.copyBean(dto, SysUser.class);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(sysUser.getUsername(), sysUser.getPassword());
        Authentication authenticate = authenticationManager.authenticate(authenticationToken);
        //判断是否认证通过
        if (Objects.isNull(authenticate)) {
            throw new RuntimeException("用户名或密码错误！");
        }
        //获取userid生成token
        LoginUser loginUser = (LoginUser) authenticate.getPrincipal();
        String userId = loginUser.getSysUser().getId().toString();
        String jwt = JwtUtil.createJWT(userId);//传数据进去jwt加密,最终生成一个字符串类型的jwt
        //把用户信息存入Redis (格式为 blogLogin：id)，设置过期时间与JWT一致
        redisCache.setCacheObject("blogLogin:" + userId, loginUser, JwtUtil.JWT_TTL.intValue(), TimeUnit.MILLISECONDS);

        //把token和userInfo封装，返回
        //把User转化成UserInfoVo
        SysUser sysUser1 = loginUser.getSysUser();
        SysUserInfoVo sysUserInfoVo = BeanCopyUtils.copyBean(sysUser1, SysUserInfoVo.class);
        
        // 如果昵称为空，使用用户名代替
        if (!StringUtils.hasText(sysUserInfoVo.getNickname())) {
            sysUserInfoVo.setNickname(sysUser1.getUsername());
        }
        // 如果头像为空，根据邮箱生成头像
        if (!StringUtils.hasText(sysUserInfoVo.getAvatar())) {
            sysUserInfoVo.setAvatar(GravatarUtils.getGravatarUrl(sysUser1.getEmail()));
        }
        
        // 查询用户角色
        List<String> roles = sysRoleMapper.selectRoleCodeByUserId(sysUser1.getId());
        if (roles == null || roles.isEmpty()) {
            // 如果没有角色，设置为空列表
            roles = Collections.emptyList();
        }
        sysUserInfoVo.setRoles(roles);
        
        BlogUserLoginVo vo = new BlogUserLoginVo(jwt, sysUserInfoVo);
        return ResponseResult.okResult(vo);
    }

    @Override
    public ResponseResult logout()
    {
        //获取token，解析获取userid(在哪获取token呢，去之前将token封装存储在SecurityContextHolder里面拿)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        //获取uesrid
        Long userId = loginUser.getSysUser().getId();
        //删除redis中的用户信息
        redisCache.deleteObject("blogLogin:" + userId);
        return ResponseResult.okResult();
    }
    
    //通过邮箱登录
    @Override
    public ResponseResult loginByEmail(LoginByEmailOrPhoneDto dto)
    {
        String email = dto.getEmail();
        String code = dto.getCode();
        
        // 验证邮箱和验证码不能为空
        if (!StringUtils.hasText(email)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_NOT_NULL);
        }
        if (!StringUtils.hasText(code)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码不能为空");
        }
        
        // 从Redis获取验证码
        String redisKey = "login:code:email:" + email;
        String redisCode = redisCache.getCacheObject(redisKey);
        
        // 验证码不存在或已过期
        if (!StringUtils.hasText(redisCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.VERIFICATION_CODE_ERROR);
        }
        
        // 验证码错误
        if (!code.equals(redisCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.VERIFICATION_CODE_ERROR);
        }
        
        // 验证通过，立即删除Redis中的验证码（防止重复使用）
        redisCache.deleteObject(redisKey);
        
        // 查询用户信息
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getEmail, email);
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
        
        if (sysUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "用户不存在");
        }
        
        // 生成token并返回用户信息
        return generateLoginResponse(sysUser);
    }
    
    //通过手机登录
    @Override
    public ResponseResult loginByPhone(LoginByEmailOrPhoneDto dto)
    {
        String phone = dto.getPhone();
        String code = dto.getCode();
        
        // 验证手机号和验证码不能为空
        if (!StringUtils.hasText(phone)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "手机号不能为空");
        }
        if (!StringUtils.hasText(code)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码不能为空");
        }
        
        // 验证手机号格式
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_FORMAT_ERROR);
        }
        
        // 从Redis获取验证码
        String redisKey = "login:code:phone:" + phone;
        String redisCode = redisCache.getCacheObject(redisKey);
        
        // 验证码不存在或已过期
        if (!StringUtils.hasText(redisCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.VERIFICATION_CODE_ERROR);
        }
        
        // 验证码错误
        if (!code.equals(redisCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.VERIFICATION_CODE_ERROR);
        }
        
        // 验证通过，立即删除Redis中的验证码（防止重复使用）
        redisCache.deleteObject(redisKey);
        
        // 查询用户信息
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getPhone, phone);
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
        
        if (sysUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "用户不存在");
        }
        
        // 生成token并返回用户信息
        return generateLoginResponse(sysUser);
    }
    
    //生成登录响应（token和用户信息）
    private ResponseResult generateLoginResponse(SysUser sysUser)
    {
        // 创建LoginUser对象
        LoginUser loginUser = new LoginUser(sysUser);
        
        // 生成JWT token
        String userId = sysUser.getId().toString();
        String jwt = JwtUtil.createJWT(userId);
        
        // 把用户信息存入Redis，设置过期时间与JWT一致
        redisCache.setCacheObject("blogLogin:" + userId, loginUser, JwtUtil.JWT_TTL.intValue(), TimeUnit.MILLISECONDS);
        
        // 封装返回数据
        SysUserInfoVo sysUserInfoVo = BeanCopyUtils.copyBean(sysUser, SysUserInfoVo.class);
        
        // 如果昵称为空，使用用户名代替
        if (!StringUtils.hasText(sysUserInfoVo.getNickname())) {
            sysUserInfoVo.setNickname(sysUser.getUsername());
        }
        // 如果头像为空，根据邮箱生成头像
        if (!StringUtils.hasText(sysUserInfoVo.getAvatar())) {
            sysUserInfoVo.setAvatar(GravatarUtils.getGravatarUrl(sysUser.getEmail()));
        }
        
        // 查询用户角色
        List<String> roles = sysRoleMapper.selectRoleCodeByUserId(sysUser.getId());
        if (roles == null || roles.isEmpty()) {
            // 如果没有角色，设置为空列表
            roles = Collections.emptyList();
        }
        sysUserInfoVo.setRoles(roles);
        
        BlogUserLoginVo vo = new BlogUserLoginVo(jwt, sysUserInfoVo);
        return ResponseResult.okResult(vo);
    }
}
