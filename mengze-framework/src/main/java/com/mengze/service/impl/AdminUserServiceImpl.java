package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddUserDto;
import com.mengze.domain.dto.UpdateUserDto;
import com.mengze.domain.dto.UserListDto;
import com.mengze.domain.entity.*;
import com.mengze.domain.vo.*;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysUserMapper;
import com.mengze.mapper.SysUserRoleMapper;
import com.mengze.service.*;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.ExcelExportUtil;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户表(SysUser)表服务实现类(后台)
 *
 * @author makejava
 * @since 2026-01-14 15:21:53
 */
@Service("adminUserService")
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
                            LambdaQueryWrapper<com.mengze.domain.entity.SysRoleDept> deptWrapper = new LambdaQueryWrapper<>();
                            deptWrapper.eq(com.mengze.domain.entity.SysRoleDept::getRoleId, role.getId())
                                    .select(com.mengze.domain.entity.SysRoleDept::getDeptId);
                            List<com.mengze.domain.entity.SysRoleDept> roleDeptList = sysRoleDeptService.list(deptWrapper);
                            deptIds.addAll(roleDeptList.stream().map(com.mengze.domain.entity.SysRoleDept::getDeptId).toList());
                        }
                    }
                    if (!deptIds.isEmpty()) {
                        queryWrapper.in(SysUser::getDeptId, deptIds.stream().map(String::valueOf).toList());
                    }
                }
            }
        }

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
                .collect(java.util.stream.Collectors.toList());
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

    //下载导入用户模板
    @Override
    public void downloadImportUsersTemplate(HttpServletResponse response)
    {
        //生成初始数据
        try {
            List<UserImportTemplateVo> templateData = Collections.singletonList(
                    new UserImportTemplateVo(
                            "云梦泽",
                            "云梦泽",
                            "弟弟",
                            "2962933152@qq.com",
                            "18329384753",
                            "总部门",
                            "系统管理员",
                            "正常",
                            new Date()
                    )
            );

            //下载的文件命名
            String fileName = "用户导入模板_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            ExcelExportUtil.exportExcel(response, templateData, UserImportTemplateVo.class, fileName, "用户导入模板");
        } catch (Exception e) {
            throw new RuntimeException("下载用户模板失败: " + e.getMessage(), e);
        }
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

