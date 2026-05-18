package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UpdateUserInfoDto;
import com.mengze.domain.dto.UserRegisterDto;
import com.mengze.domain.entity.*;
import com.mengze.domain.vo.BloggerInfoVo;
import com.mengze.domain.vo.SysUserInfoVo;
import com.mengze.domain.vo.UploadFileMetaVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.CommentMapper;
import com.mengze.mapper.SysRoleMapper;
import com.mengze.mapper.SysUserMapper;
import com.mengze.mapper.SysUserRoleMapper;
import com.mengze.service.OssFileService;
import com.mengze.service.SensitiveUtilService;
import com.mengze.service.SysFileService;
import com.mengze.service.SysUserService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.RedisCache;
import com.mengze.utils.SecurityUtils;
import com.mengze.utils.XssUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户表(SysUser)表服务实现类(前台)
 *
 * @author makejava
 * @since 2026-01-14 15:21:53
 */
@Service("userService")
@Slf4j
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService
{

    //国内手机号正则：1开头，第二位3-9，后面9位数字
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private OssFileService ossFileService;

    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SensitiveUtilService sensitiveUtilService;

    //查询用户信息
    @Override
    public ResponseResult userInfo()
    {
        //获取当前用户id
        Long userId = SecurityUtils.getUserId();
        //根据用户id查询用户信息
        SysUser byId = getById(userId);
        //封装成UserInfo
        SysUserInfoVo vo = BeanCopyUtils.copyBean(byId, SysUserInfoVo.class);
        
        //查询用户的角色信息
        List<String> roles = getUserRoles(userId);
        vo.setRoles(roles);
        
        return ResponseResult.okResult(vo);
    }

    //更新用户信息
    @Override
    @Transactional
    public ResponseResult updateUserInfo(UpdateUserInfoDto updateUserInfoDto)
    {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //昵称不能为空
        if (!StringUtils.hasText(updateUserInfoDto.getNickname())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NICKNAME_NOT_NULL, "昵称不能为空");
        }
        
        //XSS防护：转义昵称
        String safeNickname = XssUtils.escapeHtml(updateUserInfoDto.getNickname());
        updateUserInfoDto.setNickname(safeNickname);
        
        //敏感词检测 - 昵称
        if (sensitiveUtilService.checkSensitiveWordsSimple(updateUserInfoDto.getNickname())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "昵称包含敏感词，请修改后重试");
        }

        //邮箱和电话号码不能同时为空
        if (!StringUtils.hasText(updateUserInfoDto.getEmail()) && !StringUtils.hasText(updateUserInfoDto.getPhone())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "邮箱和电话号码不能同时为空");
        }

        //校验手机号格式
        if (StringUtils.hasText(updateUserInfoDto.getPhone()) && !updateUserInfoDto.getPhone().matches(PHONE_REGEX)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_FORMAT_ERROR, "请输入正确的手机号码");
        }

        //检查手机号是否被其他用户使用
        if (StringUtils.hasText(updateUserInfoDto.getPhone())) {
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getPhone, updateUserInfoDto.getPhone())
                    .ne(SysUser::getId, currentUserId);
            SysUser existSysUser = getOne(queryWrapper);
            if (existSysUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PHONENUMBER_EXIST, "你用别人的电话号码干嘛？");
            }
        }

        //检查邮箱是否被其他用户使用
        if (StringUtils.hasText(updateUserInfoDto.getEmail())) {
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getEmail, updateUserInfoDto.getEmail())
                    .ne(SysUser::getId, currentUserId);
            SysUser existSysUser = getOne(queryWrapper);
            if (existSysUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_EXIST, "你用别人的邮箱干嘛？");
            }
        }

        //获取更新前的用户信息，用于判断头像和昵称是否变化
        SysUser oldSysUser = getById(currentUserId);
        
        //处理头像：如果是临时文件，转为正式文件
        String avatar = updateUserInfoDto.getAvatar();
        if (StringUtils.hasText(avatar) && avatar.contains("temp/")) {
            UploadFileMetaVo uploadFileMetaVo = ossFileService.moveTempToFormal(avatar);
            avatar = uploadFileMetaVo.getUrl();

            //根据当前用户和头像来源查找文件记录，存在则更新，不存在则新增
            if (StringUtils.hasText(avatar)) {
                LambdaQueryWrapper<SysFile> fileQueryWrapper = new LambdaQueryWrapper<>();
                String fileSource = "用户“" + oldSysUser.getUsername() + "”的头像图片";
                fileQueryWrapper.eq(SysFile::getCreateBy, currentUserId)
                        .eq(SysFile::getFileSource, fileSource)
                        .last("LIMIT 1");
                SysFile avatarFile = sysFileService.getOne(fileQueryWrapper);

                if (avatarFile == null) {
                    avatarFile = new SysFile();
                    avatarFile.setName(uploadFileMetaVo.getName());
                    avatarFile.setFileSource(fileSource);
                    avatarFile.setFilePath(uploadFileMetaVo.getFilePath());
                    avatarFile.setUrl(uploadFileMetaVo.getUrl());
                    avatarFile.setSize(uploadFileMetaVo.getSize());
                    avatarFile.setMimeType(uploadFileMetaVo.getMimeType());
                    avatarFile.setCreateBy(currentUserId);
                    sysFileService.save(avatarFile);
                } else {
                    avatarFile.setName(uploadFileMetaVo.getName());
                    avatarFile.setFileSource(fileSource);
                    avatarFile.setFilePath(uploadFileMetaVo.getFilePath());
                    avatarFile.setUrl(uploadFileMetaVo.getUrl());
                    avatarFile.setSize(uploadFileMetaVo.getSize());
                    avatarFile.setMimeType(uploadFileMetaVo.getMimeType());
                    avatarFile.setUpdateBy(currentUserId);
                    sysFileService.updateById(avatarFile);
                }
            }
        }

        //如果头像发生变化，删除OSS上的旧头像
        String oldAvatar = oldSysUser.getAvatar();
        if (StringUtils.hasText(avatar) && StringUtils.hasText(oldAvatar)
                && !avatar.equals(oldAvatar)) {
            try {
                ossFileService.deleteFileByUrl(oldAvatar);
                log.info("从oss中删除旧头像成功: {}", oldAvatar);
            } catch (Exception e) {
                log.warn("从oss中删除旧头像失败: {}, 错误: {}", oldAvatar, e.getMessage()); //删除失败不影响更新流程，只记录日志
            }
        }

        //使用LambdaUpdateWrapper条件更新
        LambdaUpdateWrapper<SysUser> wrapper = new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, currentUserId)
                .set(SysUser::getNickname, updateUserInfoDto.getNickname())
                .set(SysUser::getSex, updateUserInfoDto.getSex())
                .set(SysUser::getPhone, StringUtils.hasText(updateUserInfoDto.getPhone()) ? updateUserInfoDto.getPhone() : null)
                .set(SysUser::getEmail, StringUtils.hasText(updateUserInfoDto.getEmail()) ? updateUserInfoDto.getEmail() : null)
                .set(SysUser::getAvatar, avatar);

        boolean success = update(null, wrapper);
        if (!success) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "更新用户信息失败");
        }
        
        //如果头像或昵称发生变化，同步更新评论表中该用户的所有评论
        boolean avatarChanged = !avatar.equals(oldSysUser.getAvatar());
        boolean nicknameChanged = !updateUserInfoDto.getNickname().equals(oldSysUser.getNickname());
        
        if (avatarChanged || nicknameChanged) {
            LambdaUpdateWrapper<Comment> commentWrapper =
                new LambdaUpdateWrapper<Comment>()
                    .eq(Comment::getUserId, currentUserId);
            
            if (avatarChanged) {
                commentWrapper.set(Comment::getAvatar, avatar);
            }
            if (nicknameChanged) {
                commentWrapper.set(Comment::getNickname, updateUserInfoDto.getNickname());
            }
            
            commentMapper.update(null, commentWrapper);
        }
        
        return ResponseResult.okResult();
    }

    //用户注册
    @Override
    @Transactional
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

        //敏感词检测 - 用户名
        if (sensitiveUtilService.checkSensitiveWordsSimple(dto.getUsername())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "用户名包含敏感词，请修改后重试");
        }

        //XSS防护：转义用户名和昵称
        dto.setUsername(XssUtils.escapeHtml(dto.getUsername()));
        if (StringUtils.hasText(dto.getNickname())) {
            dto.setNickname(XssUtils.escapeHtml(dto.getNickname()));
        }

        //验证验证码
        ResponseResult verifyResult = verifyCode(dto);
        if (verifyResult != null) {
            return verifyResult;
        }
        
        //用户名，手机号，邮箱不能和数据库中原有的数据重复
        if (StringUtils.hasText(dto.getUsername())) {
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getUsername, dto.getUsername());
            SysUser existSysUser = getOne(queryWrapper);
            if (existSysUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.USERNAME_EXIST, "用户名已被占用！");
            }
        }
        if (StringUtils.hasText(dto.getPhone())) {
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getPhone, dto.getPhone());
            SysUser existSysUser = getOne(queryWrapper);
            if (existSysUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PHONENUMBER_EXIST, "该手机号已被注册！");
            }
        }
        if (StringUtils.hasText(dto.getEmail())) {
            LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getEmail, dto.getEmail());
            SysUser existSysUser = getOne(queryWrapper);
            if (existSysUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_EXIST, "该邮箱已被注册！");
            }
        }
        
        //创建用户对象
        SysUser sysUser = new SysUser();
        sysUser.setUsername(dto.getUsername());
        sysUser.setEmail(dto.getEmail());
        sysUser.setPhone(dto.getPhone());
        
        //密码必须密文存储到数据库中
        String encodePassword = passwordEncoder.encode(dto.getPassword());
        sysUser.setPassword(encodePassword);
        
        //如果没有设置昵称，使用用户名作为默认昵称
        if (StringUtils.hasText(dto.getNickname())) {
            sysUser.setNickname(dto.getNickname());
        } else {
            sysUser.setNickname(dto.getUsername());
        }
        
        //设置默认头像
        String avatar = generateDefaultAvatar(dto.getEmail());
        sysUser.setAvatar(avatar);
        
        //存入数据库
        save(sysUser);
        
        //为新注册用户分配默认角色（普通用户角色）
        assignDefaultRole(sysUser.getId());
        
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

    //获取博主信息
    @Override
    public ResponseResult getBloggerInfo()
    {
        // 通过角色查询博主（查询拥有admin角色的用户）
        // 首先查询admin角色的ID
        LambdaQueryWrapper<SysRole> roleQueryWrapper = new LambdaQueryWrapper<>();
        roleQueryWrapper.eq(SysRole::getRoleCode, SystemConstants.ROLE_IS_ADMIN)
                .last("LIMIT 1");
        SysRole adminRole = sysRoleMapper.selectOne(roleQueryWrapper);
        
        if (adminRole == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SELECT_ERROR, "未找到管理员角色");
        }
        
        // 查询拥有该角色的用户
        LambdaQueryWrapper<SysUserRole> userRoleQueryWrapper = new LambdaQueryWrapper<>();
        userRoleQueryWrapper.eq(SysUserRole::getRoleId, adminRole.getId())
                .last("LIMIT 1");
        SysUserRole userRole = sysUserRoleMapper.selectOne(userRoleQueryWrapper);
        
        if (userRole == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SELECT_ERROR, "未找到博主信息");
        }
        
        // 查询用户信息
        SysUser blogger = sysUserMapper.selectById(userRole.getUserId());
        
        if (blogger == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SELECT_ERROR, "未找到博主信息");
        }
        
        // 封装成 BloggerInfoVo
        BloggerInfoVo vo = new BloggerInfoVo(
            blogger.getNickname(),
            blogger.getAvatar()
        );
        
        return ResponseResult.okResult(vo);
    }

    //查询用户的角色列表功能方法
    private List<String> getUserRoles(Long userId)
    {
        //通过用户ID查询用户角色关联表
        LambdaQueryWrapper<SysUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> userRoles = sysUserRoleMapper.selectList(userRoleWrapper);

        //如果用户没有角色，返回空列表
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }

        //获取角色ID列表
        List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

        //查询角色信息
        List<SysRole> sysRoles = sysRoleMapper.selectBatchIds(roleIds);

        //返回角色代码列表
        return sysRoles.stream()
                .map(SysRole::getRoleCode)
                .collect(Collectors.toList());
    }
    
    //为新注册用户分配默认角色功能方法
    private void assignDefaultRole(Long userId)
    {
        try {
            //查询"普通用户"角色（role_code = "user"）
            LambdaQueryWrapper<SysRole> roleQueryWrapper = new LambdaQueryWrapper<>();
            roleQueryWrapper.eq(SysRole::getRoleCode, SystemConstants.ROLE_IS_USER)
                    .last("LIMIT 1");
            SysRole userRole = sysRoleMapper.selectOne(roleQueryWrapper);
            
            if (userRole != null) {
                //创建用户角色关联记录
                SysUserRole sysUserRole = new SysUserRole();
                sysUserRole.setUserId(userId);
                sysUserRole.setRoleId(userRole.getId());
                sysUserRoleMapper.insert(sysUserRole);
                log.info("为用户 {} 分配默认角色 {} 成功", userId, userRole.getName());
            } else {
                log.warn("未找到默认角色（role_code='user'），用户 {} 未分配角色", userId);
            }
        } catch (Exception e) {
            log.error("为用户 {} 分配默认角色失败: {}", userId, e.getMessage(), e);
        }
    }
}



