package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.ResetPasswordDto;
import com.mengze.domain.dto.VerifyResetCodeDto;
import com.mengze.domain.entity.SysUser;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysUserMapper;
import com.mengze.service.ResetPasswordService;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 公用密码接口服务类
 */
@Service
public class ResetResetPasswordServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ResetPasswordService
{

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private SysUserMapper sysUserMapper;

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
    @Transactional
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
