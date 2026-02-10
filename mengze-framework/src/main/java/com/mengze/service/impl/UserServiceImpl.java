package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UserRegisterDto;
import com.mengze.domain.vo.UserInfoVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.CommentMapper;
import com.mengze.mapper.UserMapper;
import com.mengze.domain.entity.User;
import com.mengze.service.UserService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.RedisCache;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户表(User)表服务实现类
 *
 * @author makejava
 * @since 2026-01-14 15:21:53
 */
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService
{

    //查询用户信息
    @Override
    public ResponseResult userInfo()
    {
        //获取当前用户id
        Long userId = SecurityUtils.getUserId();
        //根据用户id查询用户信息
        User byId = getById(userId);
        //封装成UserInfo
        UserInfoVo vo = BeanCopyUtils.copyBean(byId, UserInfoVo.class);
        return ResponseResult.okResult(vo);
    }

    //国内手机号正则：1开头，第二位3-9，后面9位数字
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    //更新用户信息
    @Override
    public ResponseResult updateUserInfo(User user)
    {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //昵称不能为空
        if (!StringUtils.hasText(user.getNickname())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NICKNAME_NOT_NULL, "昵称不能为空");
        }

        //邮箱和电话号码不能同时为空
        if (!StringUtils.hasText(user.getEmail()) && !StringUtils.hasText(user.getPhone())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "邮箱和电话号码不能同时为空");
        }

        //校验手机号格式
        if (StringUtils.hasText(user.getPhone()) && !user.getPhone().matches(PHONE_REGEX)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_FORMAT_ERROR, "请输入正确的手机号码");
        }

        //检查手机号是否被其他用户使用
        if (StringUtils.hasText(user.getPhone())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, user.getPhone())
                    .ne(User::getId, currentUserId);
            User existUser = getOne(queryWrapper);
            if (existUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_OCCUPIED, "你用别人的电话号码干嘛？");
            }
        }

        //检查邮箱是否被其他用户使用
        if (StringUtils.hasText(user.getEmail())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, user.getEmail())
                    .ne(User::getId, currentUserId);
            User existUser = getOne(queryWrapper);
            if (existUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_OCCUPIED, "你用别人的邮箱干嘛？");
            }
        }

        //检查昵称是否被其他用户使用
        if (StringUtils.hasText(user.getNickname())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getNickname, user.getNickname())
                    .ne(User::getId, currentUserId);
            User existUser = getOne(queryWrapper);
            if (existUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.NICKNAME_OCCUPIED, "这个昵称和别人的重复啦！");
            }
        }

        //获取更新前的用户信息，用于判断头像和昵称是否变化
        User oldUser = getById(currentUserId);
        
        //使用LambdaUpdateWrapper条件更新
        LambdaUpdateWrapper<User> wrapper = new LambdaUpdateWrapper<User>()
                .eq(User::getId, currentUserId)
                .set(User::getNickname, user.getNickname())
                .set(User::getSex, user.getSex())
                .set(User::getPhone, StringUtils.hasText(user.getPhone()) ? user.getPhone() : null)
                .set(User::getEmail, StringUtils.hasText(user.getEmail()) ? user.getEmail() : null)
                .set(User::getAvatar, user.getAvatar());

        boolean success = update(null, wrapper);
        if (!success) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "更新用户信息失败");
        }
        
        //如果头像或昵称发生变化，同步更新评论表中该用户的所有评论
        boolean avatarChanged = !user.getAvatar().equals(oldUser.getAvatar());
        boolean nicknameChanged = !user.getNickname().equals(oldUser.getNickname());
        
        if (avatarChanged || nicknameChanged) {
            LambdaUpdateWrapper<com.mengze.domain.entity.Comment> commentWrapper = 
                new LambdaUpdateWrapper<com.mengze.domain.entity.Comment>()
                    .eq(com.mengze.domain.entity.Comment::getUserId, currentUserId);
            
            if (avatarChanged) {
                commentWrapper.set(com.mengze.domain.entity.Comment::getAvatar, user.getAvatar());
            }
            if (nicknameChanged) {
                commentWrapper.set(com.mengze.domain.entity.Comment::getNickname, user.getNickname());
            }
            
            commentMapper.update(null, commentWrapper);
        }
        
        return ResponseResult.okResult();
    }

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private CommentMapper commentMapper;

    //用户注册
    @Override
    public ResponseResult userRegister(UserRegisterDto dto)
    {
        //对用户名和密码进行非空判断
        if (!StringUtils.hasText(dto.getUsername())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.USERNAME_NOT_NULL, "用户名不能为空");
        }
        if (!StringUtils.hasText(dto.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PASSWORD_NOT_NULL, "密码不能为空");
        }
        
        //验证码不能为空
        if (!StringUtils.hasText(dto.getCode())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码不能为空");
        }
        
        //邮箱和手机号至少要有一个
        if (!StringUtils.hasText(dto.getEmail()) && !StringUtils.hasText(dto.getPhone())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "邮箱和手机号至少填写一个");
        }
        
        //校验手机号格式
        if (StringUtils.hasText(dto.getPhone()) && !dto.getPhone().matches(PHONE_REGEX)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_FORMAT_ERROR, "请输入正确的手机号码");
        }
        
        //验证验证码
        ResponseResult verifyResult = verifyCode(dto);
        if (verifyResult != null) {
            return verifyResult;
        }
        
        //用户名，手机号，邮箱不能和数据库中原有的数据重复
        if (StringUtils.hasText(dto.getUsername())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, dto.getUsername());
            User existUser = getOne(queryWrapper);
            if (existUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.USERNAME_OCCUPIED, "用户名已被占用！");
            }
        }
        if (StringUtils.hasText(dto.getPhone())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, dto.getPhone());
            User existUser = getOne(queryWrapper);
            if (existUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_OCCUPIED, "该手机号已被注册！");
            }
        }
        if (StringUtils.hasText(dto.getEmail())) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, dto.getEmail());
            User existUser = getOne(queryWrapper);
            if (existUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_OCCUPIED, "该邮箱已被注册！");
            }
        }
        
        //创建用户对象
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        
        //密码必须密文存储到数据库中
        String encodePassword = passwordEncoder.encode(dto.getPassword());
        user.setPassword(encodePassword);
        
        //如果没有设置昵称，使用用户名作为默认昵称
        if (StringUtils.hasText(dto.getNickname())) {
            user.setNickname(dto.getNickname());
        } else {
            user.setNickname(dto.getUsername());
        }
        
        //设置默认头像
        String avatar = generateDefaultAvatar(dto.getEmail());
        user.setAvatar(avatar);
        
        //存入数据库
        save(user);
        return ResponseResult.okResult("注册成功");
    }
    
    //生成默认头像
    private String generateDefaultAvatar(String email)
    {
        if (StringUtils.hasText(email)) {
            //检查是否是QQ邮箱
            if (email.matches("^\\d+@qq\\.com$")) {
                //提取QQ号
                String qqNumber = email.substring(0, email.indexOf("@"));
                //返回QQ头像URL
                return "https://q1.qlogo.cn/g?b=qq&nk=" + qqNumber + "&s=100";
            }
            //其他邮箱使用Gravatar
            return com.mengze.utils.GravatarUtils.getGravatarUrl(email);
        }
        //如果没有邮箱，返回默认Gravatar
        return com.mengze.utils.GravatarUtils.getGravatarUrl("");
    }
    
    //验证注册验证码
    private ResponseResult verifyCode(UserRegisterDto dto)
    {
        String code = dto.getCode();
        String redisKey;
        String account;
        
        // 判断是邮箱注册还是手机注册
        if (StringUtils.hasText(dto.getEmail())) {
            account = dto.getEmail();
            redisKey = "register:code:email:" + account;
        } else if (StringUtils.hasText(dto.getPhone())) {
            account = dto.getPhone();
            redisKey = "register:code:phone:" + account;
        } else {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "邮箱和手机号至少填写一个");
        }
        
        // 从Redis获取验证码
        String redisCode = redisCache.getCacheObject(redisKey);
        
        // 验证码不存在或已过期
        if (!StringUtils.hasText(redisCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.VERIFICATION_CODE_ERROR, "验证码错误或已过期");
        }
        
        // 验证码错误
        if (!code.equals(redisCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.VERIFICATION_CODE_ERROR, "验证码错误");
        }
        
        // 验证通过，立即删除Redis中的验证码（防止重复使用）
        redisCache.deleteObject(redisKey);
        
        return null;
    }
}

