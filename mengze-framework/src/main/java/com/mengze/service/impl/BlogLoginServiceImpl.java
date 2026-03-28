package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LoginDto;
import com.mengze.domain.dto.ResetPasswordDto;
import com.mengze.domain.dto.UserLoginDto;
import com.mengze.domain.dto.VerifyResetCodeDto;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.SysUser;
import com.mengze.domain.vo.BlogUserLoginVo;
import com.mengze.domain.vo.SysUserInfoVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import com.mengze.mapper.SysUserMapper;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

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
    private PasswordEncoder passwordEncoder;

    @Override
    public ResponseResult login(UserLoginDto dto)
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
        //把用户信息存入Redis (格式为 bloglogin：id)
        redisCache.setCacheObject("bloglogin:" + userId, loginUser);

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
        redisCache.deleteObject("bloglogin:" + userId);
        return ResponseResult.okResult();
    }
    
    //通过邮箱登录
    @Override
    public ResponseResult loginByEmail(LoginDto dto)
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
    public ResponseResult loginByPhone(LoginDto dto)
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
        
        // 把用户信息存入Redis
        redisCache.setCacheObject("bloglogin:" + userId, loginUser);
        
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
        
        BlogUserLoginVo vo = new BlogUserLoginVo(jwt, sysUserInfoVo);
        return ResponseResult.okResult(vo);
    }

    //验证重置验证码
    @Override
    public ResponseResult verifyResetCode(VerifyResetCodeDto dto)
    {
        //邮箱不能为空
        if (!StringUtils.hasText(dto.getEmail())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_NOT_NULL);
        }

        //验证码不能为空
        if (!StringUtils.hasText(dto.getCode())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码不能为空");
        }

        //从Redis获取验证码
        String redisKey = "forgot_password:code:email:" + dto.getEmail();
        String redisCode = redisCache.getCacheObject(redisKey);

        //验证码不存在或已过期
        if (!StringUtils.hasText(redisCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.VERIFICATION_CODE_ERROR, "验证码错误或已过期");
        }

        //验证码错误
        if (!dto.getCode().equals(redisCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.VERIFICATION_CODE_ERROR, "验证码错误");
        }

        return ResponseResult.okResult("验证成功");
    }

    //重置密码
    @Override
    public ResponseResult resetPassword(ResetPasswordDto dto)
    {
        //邮箱不能为空
        if (!StringUtils.hasText(dto.getEmail())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_NOT_NULL);
        }

        //验证码不能为空
        if (!StringUtils.hasText(dto.getCode())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码不能为空");
        }

        //新密码不能为空
        if (!StringUtils.hasText(dto.getNewPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PASSWORD_NOT_NULL);
        }

        //新密码至少6位
        if (dto.getNewPassword().length() < 6) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "密码至少6位");
        }

        //从Redis获取验证码
        String redisKey = "forgot_password:code:email:" + dto.getEmail();
        String redisCode = redisCache.getCacheObject(redisKey);

        //验证码不存在或已过期
        if (!StringUtils.hasText(redisCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.VERIFICATION_CODE_ERROR, "验证码错误或已过期");
        }

        //验证码错误
        if (!dto.getCode().equals(redisCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.VERIFICATION_CODE_ERROR, "验证码错误");
        }

        //查询用户是否存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getEmail, dto.getEmail());
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);

        if (sysUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该邮箱未注册");
        }

        //验证新密码不能与旧密码相同
        if (passwordEncoder.matches(dto.getNewPassword(), sysUser.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "新密码不能与旧密码相同");
        }

        //加密新密码
        String encodePassword = passwordEncoder.encode(dto.getNewPassword());

        //更新密码
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, sysUser.getId())
                .set(SysUser::getPassword, encodePassword);

        int result = sysUserMapper.update(null, updateWrapper);

        if (result == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "密码重置失败");
        }

        //密码重置成功后，删除Redis中的验证码（防止重复使用）
        redisCache.deleteObject(redisKey);

        return ResponseResult.okResult("密码重置成功");
    }
}
