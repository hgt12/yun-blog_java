package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.SysDept;
import com.mengze.domain.entity.SysRole;
import com.mengze.domain.entity.SysRoleDept;
import com.mengze.domain.vo.DeptOptionVo;
import com.mengze.mapper.SysDeptMapper;
import com.mengze.service.SysDeptService;
import com.mengze.service.SysRoleDeptService;
import com.mengze.service.SysRoleService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 部门表(SysDept)表服务实现类
 *
 * @author makejava
 * @since 2026-05-15 13:06:17
 */
@Service("sysDeptService")
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService
{
    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysRoleDeptService sysRoleDeptService;

    //获取部门下拉选项（树结构）
    @Override
    public ResponseResult<List<DeptOptionVo>> getDeptOptions()
    {
        //构建查询条件
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();

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

            //判断用户权限大小
            String deptId = loginUser.getSysUser().getDeptId();
            //超级管理员可以看到所有是数据
            if (!Objects.equals(maxDataScope, 1))
            {
                //可查看本部门及子部门
                if (Objects.equals(maxDataScope, 2)) {
                    //如果当前用户本身绑定了部门就查出本部门以及所有子部门
                    if (deptId != null && !deptId.isBlank()) {
                        List<Long> deptIds = selectDeptAndChildrenIds(Long.valueOf(deptId));
                        queryWrapper.in(SysDept::getId, deptIds);
                    } else {
                        //如果当前用户没有部门信息则只能看自己创建的数据
                        queryWrapper.eq(SysDept::getCreateBy, currentUserId);
                    }
                } else if (Objects.equals(maxDataScope, 3)) {
                    //只能查看本部门数据
                    if (deptId != null && !deptId.isBlank()) {
                        queryWrapper.eq(SysDept::getId, Long.valueOf(deptId));
                    } else {
                        //如果当前用户没有部门信息则只能看自己创建的数据
                        queryWrapper.eq(SysDept::getCreateBy, currentUserId);
                    }
                } else if (Objects.equals(maxDataScope, 4)) {
                    //只能查看本人创建的数据
                    queryWrapper.eq(SysDept::getCreateBy, currentUserId);
                } else if (Objects.equals(maxDataScope, 5)) {
                    //表示自定义部门权限
                    //先遍历用户拥有的角色 把所有自定义部门权限对应的部门都查出来
                    List<Long> deptIds = new ArrayList<>();
                    for (SysRole role : roles) {
                        if (Objects.equals(role.getDataScope(), 5)) {
                            deptIds.addAll(selectDeptAndChildrenIdsByRole(role.getId()));
                        }
                    }

                    //如果查到了对应部门就按部门进行过滤
                    if (!deptIds.isEmpty()) {
                        queryWrapper.in(SysDept::getId, deptIds);
                    } else {
                        //如果没有配置任何自定义部门权限则只能看自己创建的数据
                        queryWrapper.eq(SysDept::getCreateBy, currentUserId);
                    }
                }
            }
        }

        //查询状态正常的部门,按排序降序
        queryWrapper.eq(SysDept::getStatus, SystemConstants.STATUS_NORMAL)
                    .orderByAsc(SysDept::getSort);

        //查询所有符合条件的部门
        List<SysDept> sysDepts = list(queryWrapper);

        //如果不是超级管理员且查出来的部门不包含根节点，则补上根节点，保证树能正常展开
        if (!Objects.equals(currentUserId, 1L)) {
            sysDepts = addParentDepts(sysDepts);
        }

        //封装成vo
        List<DeptOptionVo> deptOptionVos = BeanCopyUtils.copyBeanList(sysDepts, DeptOptionVo.class);

        //构建菜单树
        List<DeptOptionVo> deptOptionTree = builderDeptOptionsTree(deptOptionVos, 0L);

        return ResponseResult.okResult(deptOptionTree);
    }

    @Override
    public List<Long> selectDeptAndChildrenIds(Long deptId)
    {
        List<Long> deptIds = new ArrayList<>();
        collectDeptAndChildrenIds(deptId, deptIds);
        return deptIds;
    }

    //添加父部门
    private List<SysDept> addParentDepts(List<SysDept> sysDepts)
    {
        List<SysDept> allDepts = new ArrayList<>(sysDepts);
        for (SysDept dept : sysDepts) {
            collectParentDepts(dept.getParentId(), allDepts);
        }
        return allDepts.stream().distinct().collect(Collectors.toList());
    }

    //收集父部门
    private void collectParentDepts(Long parentId, List<SysDept> allDepts)
    {
        if (parentId == null || parentId == 0L) {
            return;
        }
        SysDept parent = getById(parentId);
        if (parent != null && allDepts.stream().noneMatch(item -> Objects.equals(item.getId(), parent.getId()))) {
            allDepts.add(parent);
            collectParentDepts(parent.getParentId(), allDepts);
        }
    }

    //根据当前角色查询部门id
    private List<Long> selectDeptAndChildrenIdsByRole(Long roleId)
    {
        LambdaQueryWrapper<SysRoleDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleDept::getRoleId, roleId)
               .select(SysRoleDept::getDeptId);
        List<SysRoleDept> roleDepts = sysRoleDeptService.list(wrapper);
        List<Long> deptIds = new ArrayList<>();
        for (SysRoleDept roleDept : roleDepts) {
            deptIds.addAll(selectDeptAndChildrenIds(roleDept.getDeptId()));
        }
        return deptIds;
    }

    //查询部门与子部门id
    private void collectDeptAndChildrenIds(Long deptId, List<Long> deptIds)
    {
        deptIds.add(deptId);
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDept::getParentId, deptId)
                .eq(SysDept::getStatus, SystemConstants.STATUS_NORMAL);
        List<SysDept> children = list(queryWrapper);
        for (SysDept child : children) {
            collectDeptAndChildrenIds(child.getId(), deptIds);
        }
    }



    //创建DeptOptions的菜单树
    private List<DeptOptionVo> builderDeptOptionsTree(List<DeptOptionVo> deptOptionVos, Long parentId)
    {
        return deptOptionVos.stream()
                .filter(deptOptionVo -> parentId.equals(deptOptionVo.getParentId()))
                .map(menu -> menu.setChildren(getChildrenByDeptOptions(menu, deptOptionVos)))
                .collect(Collectors.toList());
    }

    //获取传入参数的 子部门集合
    private List<DeptOptionVo> getChildrenByDeptOptions(DeptOptionVo deptOptionVo, List<DeptOptionVo> deptOptionVos)
    {
        return deptOptionVos.stream()
                .filter(child -> deptOptionVo.getId().equals(child.getParentId()))
                .map(child -> child.setChildren(getChildrenByDeptOptions(child, deptOptionVos)))
                .collect(Collectors.toList());
    }
}

