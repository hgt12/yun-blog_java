package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.UserListDto;
import com.mengze.domain.entity.*;
import com.mengze.domain.vo.*;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysUserMapper;
import com.mengze.mapper.SysUserRoleMapper;
import com.mengze.service.*;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
    private SysUserService sysUserService;


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
        Long currentUserId = loginUser.getSysUser().getId();

        //超级管理员直接看全部；普通用户按数据权限限制
        if (!Objects.equals(currentUserId, 1L)) {
            List<SysRole> roles = sysRoleService.selectRolesByUserId(currentUserId);
            Integer maxDataScope = roles.stream()
                    .map(SysRole::getDataScope)
                    .filter(Objects::nonNull)
                    .min(Integer::compareTo)
                    .orElse(4);

            // 1-全部数据，直接不加限制
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
        SysUser sysUser = sysUserService.getById(userId);
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

