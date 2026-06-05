package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.OssFileConstants;
import com.mengze.constants.RedisKeyConstants;
import com.mengze.constants.ResultPutConstants;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.*;
import com.mengze.domain.entity.*;
import com.mengze.domain.vo.*;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.CommentMapper;
import com.mengze.mapper.SysUserMapper;
import com.mengze.mapper.SysUserRoleMapper;
import com.mengze.service.*;
import com.mengze.utils.*;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 用户表(SysUser)表服务实现类(后台)
 *
 * @author makejava
 * @since 2026-01-14 15:21:53
 */
@Service("adminUserService")
@Slf4j
public class AdminUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements AdminUserService
{
    @Autowired
    private SysMenuService sysMenuService;

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private SysRoleDeptService sysRoleDeptService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private OssFileService ossFileService;

    @Autowired
    private SensitiveUtilService sensitiveUtilService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private AliyunSmsServiceImpl aliyunSmsServiceImpl;

    @Autowired
    private EmailServiceImpl emailServiceImpl;

    @Override
    public ResponseResult getAdminInfo()
    {
        //获取当前用户id
        LoginUser loginUser = SecurityUtils.getLoginUser();

        //根据用户id查询用户权限信息
        List<String> perms = sysMenuService.selectPermByUserId(loginUser.getSysUser().getId());
        //根据用户id查询用户角色信息
        List<String> roleCode = sysRoleService.selectRoleCodeByUserId(loginUser.getSysUser().getId());

        //获取用户信息
        SysUser sysUser = loginUser.getSysUser();
        SysUserInfoVo sysUserInfoVo = BeanCopyUtils.copyBean(sysUser, SysUserInfoVo.class);

        AdminUserInfoVo adminUserInfoVo = new AdminUserInfoVo(sysUserInfoVo, roleCode, perms);

        return ResponseResult.okResult(adminUserInfoVo);
    }

    //获取用户列表
    @Override
    public ResponseResult getUserList(Integer pageNum, Integer pageSize, UserListDto userListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        //获取当前用户拥有的数据权限范围
        applyDataScopeFilter(queryWrapper);

        //根据关键子查询（用户名，昵称，手机号）
        if (StringUtils.hasText(userListDto.getKeywords())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysUser::getUsername, userListDto.getKeywords())
                    .or()
                    .like(SysUser::getNickname, userListDto.getKeywords())
                    .or()
                    .like(SysUser::getPhone, userListDto.getKeywords())
            );
        }

        //根据状态筛选
        if (StringUtils.hasText(userListDto.getStatus())){
            queryWrapper.eq(SysUser::getStatus, userListDto.getStatus());
        }

        //按部门id查询
        if (StringUtils.hasText(userListDto.getDeptId())) {
            queryWrapper.eq(SysUser::getDeptId, userListDto.getDeptId());
        }

        //按创建时间查询
        if (userListDto.getCreateTime() != null && userListDto.getCreateTime().size() == 2)
        {
            //补全当天完整时间，因为前端只传 2026-04-01 不完整
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Date startTime = sdf.parse(userListDto.getCreateTime().get(0) + " 00:00:00");
                Date endTime = sdf.parse(userListDto.getCreateTime().get(1) + " 23:59:59");

                queryWrapper.between(SysUser::getCreateTime, startTime, endTime);
            } catch (ParseException e) {
                throw new RuntimeException("创建时间格式不正确", e);
            }
        }

        //根据创建时间降序排序
        queryWrapper.orderByDesc(SysUser::getCreateTime);

        //封装分页
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        page(page, queryWrapper);

        //封装成vo返回
        List<AdminUserListVo> adminUserListVos = page.getRecords().stream()
                .map(sysUser -> {
                    AdminUserListVo vo = BeanCopyUtils.copyBean(sysUser, AdminUserListVo.class);
                    if (StringUtils.hasText(sysUser.getDeptId())) {
                        SysDept dept = null;
                        try {
                            dept = sysDeptService.getById(Long.valueOf(sysUser.getDeptId()));
                        } catch (NumberFormatException ignored) {
                            // 部门ID不是数字时忽略部门名称回填
                        }
                        if (dept != null) {
                            vo.setDeptName(dept.getName());
                        }
                    }

                    //查询是否为管理员
                    List<String> roleNames = sysRoleService.selectRoleNameByUserId(sysUser.getId());
                    if (roleNames != null && !roleNames.isEmpty()) {
                        vo.setRoleNames(String.join(",", roleNames));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
        PageVo pageVo = new PageVo(adminUserListVos, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }

    //获取用户表单数据
    @Override
    public ResponseResult<UserFormDetailVo> getUserFormDetail(Long userId)
    {
        //检查用户是否存在
        SysUser sysUser = adminUserService.getById(userId);
        if (sysUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该用户不存在！");
        }

        //转换vo
        UserFormDetailVo userFormDetailVo = BeanCopyUtils.copyBean(sysUser, UserFormDetailVo.class);

        //填充角色id列表
        List<Long> roleIds = getRoleIdsByUserId(userId);
        userFormDetailVo.setRoleIds(roleIds);

        return ResponseResult.okResult(userFormDetailVo);
    }

    //添加用户
    @Override
    public ResponseResult addUser(AddUserDto addUserDto)
    {
        //检查用户名是否已经存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, addUserDto.getUsername());
        long count = adminUserService.count(queryWrapper);
        if (count > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.USERNAME_EXIST, "用户名重复，请使用其他用户名！");
        }

        //检查手机号是否已经存在 因为手机号和邮箱不是必填项 所以用 StringUtils.hasText() 来防止查空数据
        if (StringUtils.hasText(addUserDto.getMobile())) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getPhone, addUserDto.getMobile());
            count = adminUserService.count(queryWrapper);
            if (count > 0) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PHONENUMBER_EXIST, "手机号重复，请使用其他手机号！");
            }
        }

        //检查邮箱是否已经存在
        if (StringUtils.hasText(addUserDto.getEmail())) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getEmail, addUserDto.getEmail());
            count = adminUserService.count(queryWrapper);
            if (count > 0) {
                return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_EXIST, "邮箱重复，请使用其他邮箱！");
            }
        }

        //检查部门是否存在
        SysDept dept = null;
        if (StringUtils.hasText(addUserDto.getDeptId())) {
            try {
                dept = sysDeptService.getById(Long.valueOf(addUserDto.getDeptId()));
            } catch (NumberFormatException e) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "部门ID格式不正确");
            }
        }
        if (dept == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "部门不存在！");
        }

        //检查角色是否存在，且不能包含系统超级管理员角色
        for (Long roleId : addUserDto.getRoleIds())
        {
            SysRole role = sysRoleService.getById(roleId);
            if (role == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "角色不存在！");
            }
            if (Objects.equals(role.getId(), 1L)) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "不能分配系统超级管理员角色！");
            }
        }

        //新增用户并设置默认密码并加密后入库
        SysUser user = BeanCopyUtils.copyBean(addUserDto, SysUser.class);
        user.setPassword(new BCryptPasswordEncoder().encode("user123456"));

        boolean save = adminUserService.save(user);
        if (!save) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "新增用户失败！");
        }

        //保存用户角色关联
        List<SysUserRole> userRoles = addUserDto.getRoleIds().stream()
                .map(roleId -> {
                    SysUserRole userRole = new SysUserRole();
                    userRole.setUserId(user.getId());
                    userRole.setRoleId(roleId);
                    userRole.setCreateTime(new Date());
                    return userRole;
                })
                .collect(Collectors.toList());

        for (SysUserRole userRole : userRoles) {
            sysUserRoleMapper.insert(userRole);
        }

        return ResponseResult.okResult();
    }

    //更新用户
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateUser(Long id, UpdateUserDto updateUserDto)
    {
        //当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long currentUserId = null;
        if (loginUser != null) {
            currentUserId = loginUser.getSysUser().getId();
        }

        //检查用户是否存在
        SysUser oldUser = adminUserService.getById(id);
        if (oldUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该用户不存在！");
        }

        //超级管理员只能由自己修改自己的信息
        if (Objects.equals(oldUser.getId(), 1L) && !Objects.equals(currentUserId, 1L)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "只有超级管理员本人可以修改超级管理员信息！");
        }

        //检查用户名是否已经存在(除了自己)
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, updateUserDto.getUsername())
                .ne(SysUser::getId, id);
        long count = adminUserService.count(queryWrapper);
        if (count > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.USERNAME_EXIST, "用户名重复，请使用其他用户名！");
        }

        //检查手机号是否被其他用户使用
        if (StringUtils.hasText(updateUserDto.getMobile())) {
            LambdaQueryWrapper<SysUser> phoneWrapper = new LambdaQueryWrapper<>();
            phoneWrapper.eq(SysUser::getPhone, updateUserDto.getMobile())
                    .ne(SysUser::getId, id);
            SysUser existSysUser = adminUserService.getOne(phoneWrapper);
            if (existSysUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PHONENUMBER_EXIST, "该手机号已被占用！");
            }
        }

        //检查邮箱是否被其他用户使用
        if (StringUtils.hasText(updateUserDto.getEmail())) {
            LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
            emailWrapper.eq(SysUser::getEmail, updateUserDto.getEmail())
                    .ne(SysUser::getId, id);
            SysUser existSysUser = adminUserService.getOne(emailWrapper);
            if (existSysUser != null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_EXIST, "该邮箱已被占用!");
            }
        }

        //检查部门是否存在
        SysDept dept = null;
        if (StringUtils.hasText(updateUserDto.getDeptId())) {
            try {
                dept = sysDeptService.getById(Long.valueOf(updateUserDto.getDeptId()));
            } catch (NumberFormatException e) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "部门ID格式不正确");
            }
        }
        if (dept == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "部门不存在！");
        }

        //检查角色是否存在，且不能包含系统超级管理员角色
        for (Long roleId : updateUserDto.getRoleIds())
        {
            SysRole role = sysRoleService.getById(roleId);
            if (role == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "角色不存在！");
            }
            if (Objects.equals(role.getId(), 1L)) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "管理员是神圣不可侵犯的！");
            }
        }

        //更新用户信息
        SysUser user = BeanCopyUtils.copyBean(updateUserDto, SysUser.class);
        user.setId(id);
        boolean update = adminUserService.updateById(user);
        if (!update) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "更新用户失败！");
        }

        //如果用户状态发生变化，在用户有邮箱的前提下，会发送通知邮件（账号封禁或解封）
        boolean statusChanged = !Objects.equals(oldUser.getStatus(), user.getStatus());
        boolean statusSwitchToNormalOrDisabled = Objects.equals(oldUser.getStatus(), SystemConstants.USER_STATUS_NORMAL) || Objects.equals(user.getStatus(), SystemConstants.USER_STATUS_NORMAL);
        if (statusChanged && statusSwitchToNormalOrDisabled && StringUtils.hasText(oldUser.getEmail())) {
            if (Objects.equals(user.getStatus(), SystemConstants.ARTICLE_STATUS_NORMAL)) {
                emailService.sendUserEnabledNotification(oldUser.getEmail(), oldUser.getNickname(), oldUser.getUsername());
            } else {
                emailService.sendUserDisabledNotification(oldUser.getEmail(), oldUser.getNickname(), oldUser.getUsername());
            }
        }

        //删除原有角色关联
        LambdaQueryWrapper<SysUserRole> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(SysUserRole::getUserId, id);
        sysUserRoleMapper.delete(deleteWrapper);

        //新增角色关联
        List<SysUserRole> userRoles = updateUserDto.getRoleIds().stream()
                .map(roleId -> {
                    SysUserRole userRole = new SysUserRole();
                    userRole.setUserId(id);
                    userRole.setRoleId(roleId);
                    userRole.setCreateTime(new Date());
                    return userRole;
                })
                .collect(Collectors.toList());

        for (SysUserRole userRole : userRoles) {
            sysUserRoleMapper.insert(userRole);
        }

        return ResponseResult.okResult();
    }

    //删除用户
    @Override
    @Transactional
    public ResponseResult deleteUser(Long[] ids)
    {
        //判断用户是否存在
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的用户");
        }

        //不允许删除系统管理员用户
        List<Long> userIdList = Arrays.asList(ids);
        List<SysUser> usersToDelete = adminUserService.listByIds(userIdList);
        List<Long> adminUserIds = usersToDelete.stream()
                .map(SysUser::getId)
                .filter(id -> Objects.equals(id, 1L))
                .toList();
        if (!adminUserIds.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "系统管理员用户不能删除！");
        }

        //获取用户id列表
        if (usersToDelete.size() != ids.length)
        {
            //找出不存在的 用户id
            Set<Long> existIds = usersToDelete.stream()
                    .map(SysUser::getId)
                    .collect(Collectors.toSet());

            List<Long> notExistIds = userIdList.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败！！原因：以下用户 id 不存在：" + notExistIds);
        }

        boolean removed = adminUserService.removeByIds(userIdList);
        if (!removed) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除用户失败");
        }

        return ResponseResult.okResult();
    }

    //重置用户密码
    @Override
    public ResponseResult updateResetUserPassword(Long id, String password)
    {
        //检查用户是否存在
        SysUser user = adminUserService.getById(id);
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该用户不存在！");
        }

        //管理员密码只能由管理员本人重置
        if (Objects.equals(user.getId(), 1L) && !SecurityUtils.isAdmin()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "管理员密码只能由管理员本人重置！");
        }

        //重置密码并加密入库
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        boolean update = adminUserService.updateById(user);
        if (!update) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "重置密码失败！");
        }

        //如果用户有邮箱，则发送重置密码通知邮件给用户
        if (StringUtils.hasText(user.getEmail())) {
            emailService.sendResetPasswordNotification(
                    user.getEmail(),
                    user.getNickname(),
                    user.getUsername(),
                    password
            );
        }

        return ResponseResult.okResult();
    }

    //导出用户数据
    @Override
    public ResponseResult exportUser(Integer pageNum, Integer pageSize, UserListDto userListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        //获取当前用户拥有的数据权限范围
        applyDataScopeFilter(queryWrapper);

        //根据关键子查询（用户名，昵称，手机号）
        if (StringUtils.hasText(userListDto.getKeywords())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysUser::getUsername, userListDto.getKeywords())
                    .or()
                    .like(SysUser::getNickname, userListDto.getKeywords())
                    .or()
                    .like(SysUser::getPhone, userListDto.getKeywords())
            );
        }

        //根据状态筛选
        if (StringUtils.hasText(userListDto.getStatus())){
            queryWrapper.eq(SysUser::getStatus, userListDto.getStatus());
        }

        //按部门id查询
        if (StringUtils.hasText(userListDto.getDeptId())) {
            queryWrapper.eq(SysUser::getDeptId, userListDto.getDeptId());
        }

        //按创建时间查询
        if (userListDto.getCreateTime() != null && userListDto.getCreateTime().size() == 2)
        {
            //补全当天完整时间，因为前端只传 2026-04-01 不完整
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Date startTime = sdf.parse(userListDto.getCreateTime().get(0) + " 00:00:00");
                Date endTime = sdf.parse(userListDto.getCreateTime().get(1) + " 23:59:59");

                queryWrapper.between(SysUser::getCreateTime, startTime, endTime);
            } catch (ParseException e) {
                throw new RuntimeException("创建时间格式不正确", e);
            }
        }

        //根据创建时间降序排序
        queryWrapper.orderByDesc(SysUser::getCreateTime);

        //根据是否传入分页参数决定查询方式
        List<SysUser> users;
        if (pageNum != null && pageSize != null && pageNum > 0 && pageSize > 0) {
            //分页查询
            Page<SysUser> page = new Page<>(pageNum, pageSize);
            sysUserMapper.selectPage(page, queryWrapper);
            users = page.getRecords();
        } else {
            //查询所有符合条件的用户
            users = sysUserMapper.selectList(queryWrapper);
        }

        //转换为导出VO
        List<UserExportVo> userExportVos = new ArrayList<>();
        if (users != null && !users.isEmpty()) {
            userExportVos = users.stream()
                    .map(sysUser -> {
                        UserExportVo vo = BeanCopyUtils.copyBean(sysUser, UserExportVo.class);

                        //填充性别
                        vo.setSex(convertSex(sysUser.getSex()));

                        //填充用户状态
                        vo.setStatus(convertUserStatus(sysUser.getStatus()));

                        //填充部门信息
                        if (StringUtils.hasText(sysUser.getDeptId())) {
                            try {
                                SysDept dept = sysDeptService.getById(Long.valueOf(sysUser.getDeptId()));
                                if (dept != null) {
                                    vo.setDeptName(dept.getName());
                                }
                            } catch (NumberFormatException ignored) {
                                vo.setDeptName(sysUser.getDeptId());
                            }
                        }

                        //填充角色信息
                        List<String> roleNames = sysRoleService.selectRoleNameByUserId(sysUser.getId());
                        if (roleNames != null && !roleNames.isEmpty()) {
                            vo.setRoleNames(String.join(",", roleNames));
                        }

                        return vo;
                    })
                    .collect(Collectors.toList());
        }

        return ResponseResult.okResult(userExportVos);
    }

    //导入用户数据
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult importUsers(MultipartFile file)
    {
        //判断导入的文件是否为空
        if (file == null || file.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要导入的Excel文件");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "文件名不能为空");
        }

        //验证文件格式
        String lowerFilename = originalFilename.toLowerCase(Locale.ROOT);
        if (!(lowerFilename.endsWith(".xls") || lowerFilename.endsWith(".xlsx"))) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "只允许导入 .xls 或 .xlsx 格式的文件");
        }

        //验证文件大小不能超过1m
        long maxSize = 1024L * 1024L;
        if (file.getSize() > maxSize) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "文件大小不能超过 1MB");
        }

        List<String> messageList = new ArrayList<>();
        AtomicInteger validCount = new AtomicInteger();
        AtomicInteger invalidCount = new AtomicInteger();

        //读取每一行表格数据并校验每条用户数据
        try (InputStream inputStream = file.getInputStream())
        {
            EasyExcel.read(inputStream, UserImportTemplateVo.class, new AnalysisEventListener<UserImportTemplateVo>() {
                //在读取 Excel 表头时触发
                @Override
                public void invokeHead(Map<Integer, com.alibaba.excel.metadata.data.ReadCellData<?>> headMap, AnalysisContext context) {
                    //只触发但不执行操作
                }
                @Override
                public void invoke(UserImportTemplateVo data, AnalysisContext context)
                {
                    //校验导入数据的规格和内容
                    int rowNum = context.readRowHolder().getRowIndex() + 1;
                    String validationError = validateImportRow(data);
                    if (StringUtils.hasText(validationError)) {
                        invalidCount.incrementAndGet();
                        messageList.add("第" + rowNum + "行：" + validationError);
                        return;
                    }

                    //保存用户信息
                    ResponseResult importResult = saveImportedUser(data);
                    if (importResult.getCode() == 200) {
                        validCount.incrementAndGet();
                    } else {
                        invalidCount.incrementAndGet();
                        messageList.add("第" + rowNum + "行：" + importResult.getMsg());
                    }
                }

                //在整份 Excel 全部读取完成后触发
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    //只触发但不执行操作
                }
            }).sheet().doRead();
        } catch (IOException e) {
            throw new RuntimeException("导入文件读取失败", e);
        }

        Map<String, Object> result = new HashMap<>();
        result.put(ResultPutConstants.VALID_COUNT, validCount.get());
        result.put(ResultPutConstants.INVALID_COUNT, invalidCount.get());
        result.put(ResultPutConstants.MESSAGE_LIST, messageList);
        return ResponseResult.okResult(result);
    }

    //驳回用户头像
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult dismissUserAvatar(Long id)
    {
        //检查用户是否存在
        SysUser user = adminUserService.getById(id);
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该用户不存在！");
        }

        //没有头像时无需驳回
        if (!StringUtils.hasText(user.getAvatar())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该用户没有头像可驳回！");
        }

        //先保存原头像地址
        String avatarUrl = user.getAvatar();

        //清空用户头像
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, id)
                .set(SysUser::getAvatar, null);
        boolean update = adminUserService.update(updateWrapper);
        if (!update) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "驳回头像失败！");
        }

        //发送头像驳回邮件
        if (StringUtils.hasText(user.getEmail())) {
            emailService.sendUserAvatarDismissedNotification(user.getEmail(), user.getNickname(), user.getUsername());
        }

        //将正式文件移动到 deleted/
        try {
            ossFileService.moveFileToDeleted(avatarUrl);
        } catch (Exception e) {
            log.warn("驳回用户头像时移动文件到删除区失败, userId={}, avatarUrl={}", id, avatarUrl, e);
        }

        //如果数据库中存在对应文件记录，则删除文件元数据
        SysFile file = sysFileService.getOne(
                new LambdaQueryWrapper<SysFile>()
                        .eq(SysFile::getUrl, avatarUrl)
                        .last("limit 1")
        );
        if (file != null) {
            sysFileService.removeById(file.getId());
        }

        return ResponseResult.okResult();
    }

    //获取用户个人中心信息
    @Override
    public ResponseResult<UserProfileVo> getUserProfile()
    {
        //获取当前用户id
        Long userId = SecurityUtils.getUserId();

        //根据用户id查询用户信息
        SysUser user = adminUserService.getById(userId);
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "用户不存在！");
        }

        //封装成vo
        UserProfileVo profileVo = BeanCopyUtils.copyBean(user, UserProfileVo.class);

        //回填部门名称
        if (StringUtils.hasText(user.getDeptId()))
        {
            try {
                SysDept dept = sysDeptService.getById(Long.valueOf(user.getDeptId()));
                if (dept != null) {
                    profileVo.setDeptName(dept.getName());
                }
            } catch (NumberFormatException ignored) {
                profileVo.setDeptName(user.getDeptId());
            }
        }

        //返回
        return ResponseResult.okResult(profileVo);
    }

    //修改用户个人中心信息
    @Override
    @Transactional
    public ResponseResult updateUserProfile(UpdateUserProfileDto updateUserProfileDto)
    {
        //检测用户是否登录
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //昵称不能为空
        if (!StringUtils.hasText(updateUserProfileDto.getNickname())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NICKNAME_NOT_NULL, "昵称不能为空");
        }

        //XSS防护：转义昵称
        String safeNickname = XssUtils.escapeHtml(updateUserProfileDto.getNickname());
        updateUserProfileDto.setNickname(safeNickname);

        //敏感词检测 - 昵称
        if (sensitiveUtilService.checkSensitiveWordsSimple(updateUserProfileDto.getNickname())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "昵称包含敏感词，请修改后重试");
        }

        //获取更新前的用户信息，用于判断头像和昵称是否变化
        SysUser oldSysUser = getById(currentUserId);

        //处理头像：如果是临时文件，转为正式文件
        String avatar = updateUserProfileDto.getAvatar();
        if (StringUtils.hasText(avatar) && avatar.contains(OssFileConstants.TEMP_OSS_DIRECTORY_PATH_PREFIX))
        {
            UploadFileMetaVo uploadFileMetaVo = ossFileService.moveTempToFormal(avatar);
            avatar = uploadFileMetaVo.getUrl();

            //根据当前用户和头像来源查找文件记录，存在则更新，不存在则新增
            if (StringUtils.hasText(avatar))
            {
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
                .set(SysUser::getNickname, updateUserProfileDto.getNickname())
                .set(SysUser::getSex, updateUserProfileDto.getSex())
                .set(SysUser::getAvatar, avatar);

        boolean success = update(null, wrapper);
        if (!success) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "更新用户信息失败");
        }

        //如果头像或昵称发生变化，同步更新评论表中该用户的所有评论
        boolean avatarChanged = !Objects.equals(avatar, oldSysUser.getAvatar());
        boolean nicknameChanged = !Objects.equals(updateUserProfileDto.getNickname(), oldSysUser.getNickname());

        if (avatarChanged || nicknameChanged) {
            LambdaUpdateWrapper<Comment> commentWrapper =
                    new LambdaUpdateWrapper<Comment>()
                            .eq(Comment::getUserId, currentUserId);

            if (avatarChanged) {
                commentWrapper.set(Comment::getAvatar, avatar);
            }
            if (nicknameChanged) {
                commentWrapper.set(Comment::getNickname, updateUserProfileDto.getNickname());
            }

            commentMapper.update(null, commentWrapper);
        }

        return ResponseResult.okResult();
    }

    //修改个人密码
    @Override
    @Transactional
    public ResponseResult updateIndividualPassword(UpdateIndividualPasswordDto updateIndividualPasswordDto)
    {
        //验证用户是否登录
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //验证用户是否存在
        SysUser sysUser = sysUserMapper.selectById(currentUserId);
        if (sysUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "用户不存在");
        }

        //新密码不能为空
        if (!StringUtils.hasText(updateIndividualPasswordDto.getNewPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PASSWORD_NOT_NULL);
        }

        //新密码至少8位
        if (updateIndividualPasswordDto.getNewPassword().length() < 8) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "密码至少8位");
        }

        //判断旧密码是否正确
        if (!passwordEncoder.matches(updateIndividualPasswordDto.getOldPassword(), sysUser.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "旧密码错误");
        }

        //判断新密码和确认密码是否一样
        if (!Objects.equals(updateIndividualPasswordDto.getNewPassword(), updateIndividualPasswordDto.getConfirmPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "两次输入的密码不一致");
        }

        //旧密码不能和新密码相同
        if (passwordEncoder.matches(updateIndividualPasswordDto.getNewPassword(), sysUser.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "新密码不能与旧密码相同");
        }

        //加密新密码
        String encodePassword = passwordEncoder.encode(updateIndividualPasswordDto.getNewPassword());

        //更新密码
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, sysUser.getId())
                .set(SysUser::getPassword, encodePassword);

        int result = sysUserMapper.update(null, updateWrapper);

        if (result == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "密码重置失败");
        }

        return ResponseResult.okResult();
    }

    //发送手机验证码
    @Override
    public ResponseResult getMobileCode(String mobile)
    {
        //校验手机号不能为空
        if (!StringUtils.hasText(mobile)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "手机号不能为空");
        }

        mobile = mobile.trim();

        //校验手机号格式
        if (!mobile.matches("^1[3-9]\\d{9}$")) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_FORMAT_ERROR);
        }

        //记录IP地址
        String clientIp = getClientIp();
        ResponseResult limitResult = checkCodeLimit(mobile, clientIp);
        if (limitResult != null) {
            return limitResult;
        }

        //获得验证码并存入redis
        String code = generateCode();
        String redisKey = saveCodeToRedis(mobile, code, clientIp);

        try {
            aliyunSmsServiceImpl.sendVerificationCode(mobile, code);
        } catch (Exception e) {
            redisCache.deleteObject(redisKey);
            redisCache.deleteObject(String.format(RedisKeyConstants.ADMIN_CODE_SEND_TIME_KEY_TEMPLATE, mobile));
            log.error("发送手机验证码失败, mobile={}, error={}", mobile, e.getMessage(), e);
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码发送失败，请稍后重试");
        }

        return ResponseResult.okResult();
    }

    //绑定或更换手机号
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateOrBindPhone(UpdateOrBindPhoneDto updateOrBindPhoneDto)
    {
        //检测用户是否登录
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //校验手机号格式
        String mobile = updateOrBindPhoneDto.getMobile() == null ? null : updateOrBindPhoneDto.getMobile().trim();
        if (!StringUtils.hasText(mobile)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "手机号不能为空");
        }
        if (!mobile.matches("^1[3-9]\\d{9}$")) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_FORMAT_ERROR);
        }

        //验证码不能为空
        String code = updateOrBindPhoneDto.getCode() == null ? null : updateOrBindPhoneDto.getCode().trim();
        if (!StringUtils.hasText(code)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码不能为空");
        }

        //密码不能为空
        String password = updateOrBindPhoneDto.getPassword() == null ? null : updateOrBindPhoneDto.getPassword().trim();
        if (!StringUtils.hasText(password)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PASSWORD_NOT_NULL, "密码不能为空");
        }

        //验证当前用户是否存在
        SysUser currentUser = adminUserService.getById(currentUserId);
        if (currentUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "用户不存在");
        }

        //校验验证码
        String redisKey = String.format(RedisKeyConstants.ADMIN_CODE_KEY_TEMPLATE, mobile);
        String cacheCode = redisCache.getCacheObject(redisKey);
        if (!StringUtils.hasText(cacheCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码已过期，请重新获取");
        }
        if (!Objects.equals(cacheCode, code)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码错误");
        }

        //校验当前密码
        if (!passwordEncoder.matches(password, currentUser.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "密码错误");
        }

        //手机号不能被其他用户占用
        LambdaQueryWrapper<SysUser> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(SysUser::getPhone, mobile)
                .ne(SysUser::getId, currentUserId);
        SysUser existUser = adminUserService.getOne(phoneWrapper);
        if (existUser != null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONENUMBER_EXIST, "该手机号已被其他用户绑定");
        }

        //更新手机号
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, currentUserId)
                .set(SysUser::getPhone, mobile);
        boolean update = adminUserService.update(updateWrapper);
        if (!update) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "绑定手机号失败");
        }

        //删除验证码缓存
        redisCache.deleteObject(redisKey);
        redisCache.deleteObject(String.format(RedisKeyConstants.ADMIN_CODE_SEND_TIME_KEY_TEMPLATE, mobile));

        return ResponseResult.okResult();
    }

    //解绑手机号
    @Override
    public ResponseResult unbindPhone(UnbindPhoneOrEmailDto unbindPhoneOrEmailDto)
    {
        //检测用户是否登录
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //验证当前用户是否存在
        SysUser currentUser = adminUserService.getById(currentUserId);
        if (currentUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "用户不存在");
        }

        //检测当前手机号是否被绑定
        if (!StringUtils.hasText(currentUser.getPhone())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "当前用户未绑定手机号");
        }

        //密码不能为空
        if (!StringUtils.hasText(unbindPhoneOrEmailDto.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PASSWORD_NOT_NULL, "密码不能为空");
        }

        //校验当前密码
        if (!passwordEncoder.matches(unbindPhoneOrEmailDto.getPassword(), currentUser.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "密码错误");
        }

        //解绑手机号
        LambdaUpdateWrapper<SysUser> unbindPhoneWrapper = new LambdaUpdateWrapper<>();
        unbindPhoneWrapper.eq(SysUser::getId, currentUser.getId())
                .set(SysUser::getPhone, null);

        boolean success = adminUserService.update(unbindPhoneWrapper);
        if (!success) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "解绑手机号失败");
        }

        return ResponseResult.okResult();
    }

    //发送邮箱验证码
    @Override
    public ResponseResult getEmailCode(String email)
    {
        //校验邮箱不能为空
        if (!StringUtils.hasText(email)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "手机号不能为空");
        }

        email = email.trim();

        //校验邮箱格式
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_FORMAT_ERROR);
        }

        //记录IP地址
        String clientIp = getClientIp();
        ResponseResult limitResult = checkCodeLimit(email, clientIp);
        if (limitResult != null) {
            return limitResult;
        }

        //获得验证码并存入redis
        String code = generateCode();
        String redisKey = saveCodeToRedis(email, code, clientIp);

        try {
            emailServiceImpl.sendVerificationCodeByUpdateOrBind(email, code);
        } catch (Exception e) {
            redisCache.deleteObject(redisKey);
            redisCache.deleteObject(String.format(RedisKeyConstants.ADMIN_CODE_SEND_TIME_KEY_TEMPLATE, email));
            log.error("发送邮箱验证码失败, email={}, error={}", email, e.getMessage(), e);
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码发送失败，请稍后重试");
        }

        return ResponseResult.okResult();
    }

    //绑定或更换邮箱
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult updateOrBindEmail(UpdateOrBindEmailDto updateOrBindEmailDto)
    {
        //检测用户是否登录
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //校验邮箱格式
        String email = updateOrBindEmailDto.getEmail() == null ? null : updateOrBindEmailDto.getEmail().trim();
        if (!StringUtils.hasText(email)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "邮箱不能为空");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONE_FORMAT_ERROR);
        }

        //验证码不能为空
        String code = updateOrBindEmailDto.getCode() == null ? null : updateOrBindEmailDto.getCode().trim();
        if (!StringUtils.hasText(code)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码不能为空");
        }

        //密码不能为空
        String password = updateOrBindEmailDto.getPassword() == null ? null : updateOrBindEmailDto.getPassword().trim();
        if (!StringUtils.hasText(password)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PASSWORD_NOT_NULL, "密码不能为空");
        }

        //验证当前用户是否存在
        SysUser currentUser = adminUserService.getById(currentUserId);
        if (currentUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "用户不存在");
        }

        //校验验证码
        String redisKey = String.format(RedisKeyConstants.ADMIN_CODE_KEY_TEMPLATE, email);
        String cacheCode = redisCache.getCacheObject(redisKey);
        if (!StringUtils.hasText(cacheCode)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码已过期，请重新获取");
        }
        if (!Objects.equals(cacheCode, code)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "验证码错误");
        }

        //校验当前密码
        if (!passwordEncoder.matches(password, currentUser.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "密码错误");
        }

        //邮箱不能被其他用户占用
        LambdaQueryWrapper<SysUser> emailWrapper = new LambdaQueryWrapper<>();
        emailWrapper.eq(SysUser::getEmail, email)
                .ne(SysUser::getId, currentUserId);
        SysUser existUser = adminUserService.getOne(emailWrapper);
        if (existUser != null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PHONENUMBER_EXIST, "该邮箱已被其他用户绑定");
        }

        //更新邮箱
        LambdaUpdateWrapper<SysUser> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(SysUser::getId, currentUserId)
                .set(SysUser::getEmail, email);
        boolean update = adminUserService.update(updateWrapper);
        if (!update) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "绑定邮箱失败");
        }

        //删除验证码缓存
        redisCache.deleteObject(redisKey);
        redisCache.deleteObject(String.format(RedisKeyConstants.ADMIN_CODE_SEND_TIME_KEY_TEMPLATE, email));

        return ResponseResult.okResult();
    }

    //解绑邮箱
    @Override
    public ResponseResult unbindEmail(UnbindPhoneOrEmailDto unbindPhoneOrEmailDto)
    {
        //检测用户是否登录
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //验证当前用户是否存在
        SysUser currentUser = adminUserService.getById(currentUserId);
        if (currentUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "用户不存在");
        }

        //检测当前邮箱是否被绑定
        if (!StringUtils.hasText(currentUser.getEmail())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "当前用户未绑定邮箱");
        }

        //密码不能为空
        if (!StringUtils.hasText(unbindPhoneOrEmailDto.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PASSWORD_NOT_NULL, "密码不能为空");
        }

        //校验当前密码
        if (!passwordEncoder.matches(unbindPhoneOrEmailDto.getPassword(), currentUser.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "密码错误");
        }

        //解绑邮箱
        LambdaUpdateWrapper<SysUser> unbindPhoneWrapper = new LambdaUpdateWrapper<>();
        unbindPhoneWrapper.eq(SysUser::getId, currentUser.getId())
                .set(SysUser::getEmail, null);

        boolean success = adminUserService.update(unbindPhoneWrapper);
        if (!success) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "解绑邮箱失败");
        }

        return ResponseResult.okResult();
    }

    //获取用户下拉选项
    @Override
    public ResponseResult getUserOptions()
    {
        //构建查询条件
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();

        //获取当前用户拥有的数据权限范围
        applyDataScopeFilter(queryWrapper);

        //获取用户id
        //查询状态正常的用户，并按 id 倒序排序
        queryWrapper.eq(SysUser::getStatus, SystemConstants.STATUS_NORMAL)
                    .orderByDesc(SysUser::getId);

        List<SysUser> users = adminUserService.list(queryWrapper);

        //封装成vo
        List<UserOptionVo> userOptionVos = BeanCopyUtils.copyBeanList(users, UserOptionVo.class);

        return ResponseResult.okResult(userOptionVos);
    }

    //获取当前用户拥有的数据权限范围
    private void applyDataScopeFilter(LambdaQueryWrapper<SysUser> queryWrapper)
    {
        //当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long currentUserId = null;
        if (loginUser != null) {
            currentUserId = loginUser.getSysUser().getId();
        }

        //超级管理员可以看到全部 普通用户按数据权限限制
        if (!Objects.equals(currentUserId, 1L)) {
            List<SysRole> roles = sysRoleService.selectRolesByUserId(currentUserId);
            Integer maxDataScope = roles.stream()
                    .map(SysRole::getDataScope)
                    .filter(Objects::nonNull)
                    .min(Integer::compareTo)
                    .orElse(4);

            //全部数据
            if (!Objects.equals(maxDataScope, 1))
            {
                if (Objects.equals(maxDataScope, 2)) {
                    //部门及子部门数据
                    List<Long> deptIds = sysDeptService.selectDeptAndChildrenIds(Long.valueOf(loginUser.getSysUser().getDeptId()));
                    queryWrapper.in(SysUser::getDeptId, deptIds.stream().map(String::valueOf).toList());
                } else if (Objects.equals(maxDataScope, 3)) {
                    //本部门数据
                    queryWrapper.eq(SysUser::getDeptId, loginUser.getSysUser().getDeptId());
                } else if (Objects.equals(maxDataScope, 4)) {
                    //本人数据
                    queryWrapper.eq(SysUser::getCreateBy, currentUserId);
                } else if (Objects.equals(maxDataScope, 5)) {
                    //自定义部门数据
                    List<Long> deptIds = new ArrayList<>();
                    for (SysRole role : roles) {
                        if (Objects.equals(role.getDataScope(), 5)) {
                            LambdaQueryWrapper<SysRoleDept> deptWrapper = new LambdaQueryWrapper<>();
                            deptWrapper.eq(SysRoleDept::getRoleId, role.getId())
                                    .select(SysRoleDept::getDeptId);
                            List<SysRoleDept> roleDeptList = sysRoleDeptService.list(deptWrapper);
                            deptIds.addAll(roleDeptList.stream().map(SysRoleDept::getDeptId).toList());
                        }
                    }
                    if (!deptIds.isEmpty()) {
                        queryWrapper.in(SysUser::getDeptId, deptIds.stream().map(String::valueOf).toList());
                    }
                }
            }
        }
    }

    private String generateCode()
    {
        Random random = new Random();
        return String.valueOf(random.nextInt(900000) + 100000);
    }

    //获取ip地址
    private String getClientIp()
    {
        String clientIp = SystemConstants.UNKNOWN;
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                clientIp = IpUtils.getIpAddr(request);
            }
        } catch (Exception e) {
            log.warn("获取客户端IP失败: {}", e.getMessage());
        }
        return clientIp;
    }

    //对验证码发送次数进行限制
    private ResponseResult checkCodeLimit(String target, String clientIp)
    {
        //检查IP日发送次数限制
        String dayKey = String.format(RedisKeyConstants.ADMIN_IP_LIMIT_DAY_KEY_TEMPLATE, clientIp);
        Integer dayCount = redisCache.getCacheObject(dayKey);
        if (dayCount != null && dayCount >= 50) {
            return ResponseResult.errorResult(AppHttpCodeEnum.IP_REQUEST_LIMIT, "系统检测到异常行为，请24小时后再试或联系管理员");
        }

        //检查同账号发送频率限制
        String sendTimeKey = String.format(RedisKeyConstants.ADMIN_CODE_SEND_TIME_KEY_TEMPLATE, target);
        String lastSendTime = redisCache.getCacheObject(sendTimeKey);
        if (StringUtils.hasText(lastSendTime)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SMS_SEND_FREQUENTLY, "验证码发送过于频繁，请1分钟后再试");
        }

        //检查IP发送频率限制
        String minuteKey = String.format(RedisKeyConstants.ADMIN_IP_LIMIT_MINUTE_KEY_TEMPLATE, clientIp);
        Integer minuteCount = redisCache.getCacheObject(minuteKey);
        if (minuteCount != null && minuteCount >= 5) {
            return ResponseResult.errorResult(AppHttpCodeEnum.IP_REQUEST_LIMIT, "您的操作过于频繁，请1分钟后再试");
        }

        //检测每小时发送频率
        String hourKey = String.format(RedisKeyConstants.ADMIN_IP_LIMIT_HOUR_KEY_TEMPLATE, clientIp);
        Integer hourCount = redisCache.getCacheObject(hourKey);
        if (hourCount != null && hourCount >= 20) {
            return ResponseResult.errorResult(AppHttpCodeEnum.IP_REQUEST_LIMIT, "您的操作过于频繁，请1小时后再试");
        }

        return null;
    }

    //保存记录到redis
    private String saveCodeToRedis(String target, String code, String clientIp)
    {
        String redisKey = String.format(RedisKeyConstants.ADMIN_CODE_KEY_TEMPLATE, target);
        String sendTimeKey = String.format(RedisKeyConstants.ADMIN_CODE_SEND_TIME_KEY_TEMPLATE, target);
        String minuteKey = String.format(RedisKeyConstants.ADMIN_IP_LIMIT_MINUTE_KEY_TEMPLATE, clientIp);
        String hourKey = String.format(RedisKeyConstants.ADMIN_IP_LIMIT_HOUR_KEY_TEMPLATE, clientIp);
        String dayKey = String.format(RedisKeyConstants.ADMIN_IP_LIMIT_DAY_KEY_TEMPLATE, clientIp);

        Integer minuteCount = redisCache.getCacheObject(minuteKey);
        Integer hourCount = redisCache.getCacheObject(hourKey);
        Integer dayCount = redisCache.getCacheObject(dayKey);

        redisCache.setCacheObject(redisKey, code, 5, TimeUnit.MINUTES);
        redisCache.setCacheObject(sendTimeKey, String.valueOf(System.currentTimeMillis()), 1, TimeUnit.MINUTES);
        redisCache.setCacheObject(minuteKey, minuteCount == null ? 1 : minuteCount + 1, 1, TimeUnit.MINUTES);
        redisCache.setCacheObject(hourKey, hourCount == null ? 1 : hourCount + 1, 1, TimeUnit.HOURS);
        redisCache.setCacheObject(dayKey, dayCount == null ? 1 : dayCount + 1, 1, TimeUnit.DAYS);

        return redisKey;
    }

    //判断导入的规格和内容
    private String validateImportRow(UserImportTemplateVo data)
    {
        if (data == null) {
            return "导入数据为空";
        }
        if (!StringUtils.hasText(data.getUsername())) {
            return "用户名不能为空";
        }
        if (!StringUtils.hasText(data.getNickname())) {
            return "昵称不能为空";
        }
        if (StringUtils.hasText(data.getPhone()) && !data.getPhone().matches("^1[3-9]\\d{9}$")) {
            return "手机号格式不正确";
        }
        if (StringUtils.hasText(data.getEmail()) && !data.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            return "邮箱格式不正确";
        }
        return null;
    }

    //保存导入的角色并设置默认密码
    private ResponseResult saveImportedUser(UserImportTemplateVo data)
    {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, data.getUsername());
        if (adminUserService.count(queryWrapper) > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.USERNAME_EXIST, "用户名已存在");
        }

        if (StringUtils.hasText(data.getPhone())) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getPhone, data.getPhone());
            if (adminUserService.count(queryWrapper) > 0) {
                return ResponseResult.errorResult(AppHttpCodeEnum.PHONENUMBER_EXIST, "手机号已存在");
            }
        }

        if (StringUtils.hasText(data.getEmail())) {
            queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SysUser::getEmail, data.getEmail());
            if (adminUserService.count(queryWrapper) > 0) {
                return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_EXIST, "邮箱已存在");
            }
        }

        SysUser user = new SysUser();
        user.setUsername(data.getUsername());
        user.setNickname(data.getNickname());
        user.setSex(StringUtils.hasText(data.getSex()) ? data.getSex() : SystemConstants.SEX_HIDE);
        user.setEmail(data.getEmail());
        user.setPhone(data.getPhone());
        user.setPassword(new BCryptPasswordEncoder().encode("user123456"));
        user.setStatus(SystemConstants.USER_STATUS_NORMAL);
        boolean save = adminUserService.save(user);
        if (!save) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "保存用户失败");
        }

        return ResponseResult.okResult();
    }

    //转换性别
    private String convertSex(String sex)
    {
        if (Objects.equals(sex, SystemConstants.SEX_MAN)) {
            return "弟弟";
        }
        if (Objects.equals(sex, SystemConstants.SEX_WOMEN)) {
            return "妹妹";
        }
        if (Objects.equals(sex, SystemConstants.SEX_HIDE)) {
            return "隐藏";
        }
        return sex;
    }

    //转换用户状态
    private String convertUserStatus(String status)
    {
        if (Objects.equals(status, SystemConstants.USER_STATUS_NORMAL)) {
            return "正常";
        }
        if (Objects.equals(status, SystemConstants.USER_STATUS_BANNED)) {
            return "已封禁";
        }
        return status;
    }


    /**
     * 根据用户ID查询角色id列表
     * @param userId 用户ID
     * @return 角色ID列表
     */
    private List<Long> getRoleIdsByUserId(Long userId)
    {
        //查询用户角色关联表
        LambdaQueryWrapper<SysUserRole> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> UserRoles = sysUserRoleMapper.selectList(queryWrapper);

        //返回角色ID列表
        return UserRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());
    }
}

