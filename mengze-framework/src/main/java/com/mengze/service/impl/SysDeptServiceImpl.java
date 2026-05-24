package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddDeptDto;
import com.mengze.domain.dto.DeptListDto;
import com.mengze.domain.dto.UpdateDeptDto;
import com.mengze.domain.entity.*;
import com.mengze.domain.vo.DeptFormDetailVo;
import com.mengze.domain.vo.DeptListVo;
import com.mengze.domain.vo.DeptOptionVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysDeptMapper;
import com.mengze.service.SysDeptService;
import com.mengze.service.SysRoleDeptService;
import com.mengze.service.SysRoleService;
import com.mengze.service.SysUserService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
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

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private SysUserService sysUserService;

    //获取部门列表（树结构）
    @Override
    public ResponseResult getDeptList(DeptListDto deptListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();

        //根据关键字查询
        if (StringUtils.hasText(deptListDto.getKeywords())) {
            queryWrapper.like(SysDept::getName, deptListDto.getKeywords());
        }

        //根据状态查询
        if (StringUtils.hasText(deptListDto.getStatus())) {
            queryWrapper.eq(SysDept::getStatus, deptListDto.getStatus());
        }

        //当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long currentUserId = loginUser.getSysUser().getId();

        //获取当前用户拥有的数据权限范围
        applyDeptDataScopeFilter(queryWrapper);

        //按排序降序
        queryWrapper.orderByAsc(SysDept::getSort);

        //查询所有符合条件的部门
        List<SysDept> sysDepts = list(queryWrapper);

        //如果不是超级管理员且查出来的部门不包含根节点则补上根节点，保证树能正常展开
        if (!Objects.equals(currentUserId, 1L)) {
            sysDepts = addParentDepts(sysDepts);
        }

        //封装成vo
        List<DeptListVo> deptListVos = BeanCopyUtils.copyBeanList(sysDepts, DeptListVo.class);

        //构建菜单树
        List<DeptListVo> deptListTree = builderDeptListTree(deptListVos, 0L);

        return ResponseResult.okResult(deptListTree);
    }

    //获取部门下拉选项（树结构）
    @Override
    public ResponseResult<List<DeptOptionVo>> getDeptOptions()
    {
        //构建查询条件
        LambdaQueryWrapper<SysDept> queryWrapper = new LambdaQueryWrapper<>();

        //当前登录用户
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Long currentUserId = loginUser.getSysUser().getId();

        //获取当前用户拥有的数据权限范围
        applyDeptDataScopeFilter(queryWrapper);

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

    //获取部门表单详情
    @Override
    public ResponseResult<DeptFormDetailVo> getDeptFormDetail(Long id)
    {
        //获取部门id
        SysDept dept = sysDeptService.getById(id);
        if (dept == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该部门不存在！");
        }

        //封装为Vo
        DeptFormDetailVo deptFormDetailVo = BeanCopyUtils.copyBean(dept, DeptFormDetailVo.class);

        //返回
        return ResponseResult.okResult(deptFormDetailVo);
    }

    //添加部门
    @Override
    public ResponseResult addDept(AddDeptDto addDeptDto)
    {
        //检测部门名称与部门编号是否已经纯在
        LambdaQueryWrapper<SysDept> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(SysDept::getName, addDeptDto.getName());
        if (sysDeptService.count(nameWrapper) > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "部门名称已存在，请勿重复添加！");
        }

        LambdaQueryWrapper<SysDept> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(SysDept::getCode, addDeptDto.getCode());
        if (sysDeptService.count(codeWrapper) > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "部门编号已存在，请勿重复添加！");
        }

        //添加部门
        SysDept sysDept = BeanCopyUtils.copyBean(addDeptDto, SysDept.class);
        Long parentId = addDeptDto.getParentId();
        sysDept.setParentId(parentId);

        //顶级部门设置 treePath
        if (Objects.equals(parentId, 0L)) {
            sysDept.setTreePath(SystemConstants.PARENT_NODE_PATH);//如果当前部门是顶级部门直接赋值 0
        } else {
            SysDept parentDept = sysDeptService.getById(parentId);//如果不是顶级部门就先查父部门是否存在
            if (parentDept == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "上级部门不存在！");
            }
            String parentTreePath = parentDept.getTreePath();//再取父部门的路径再用","拼接出当前部门完整的层级路径
            if (parentTreePath == null || parentTreePath.isBlank()) {
                parentTreePath = SystemConstants.PARENT_NODE_PATH;
            }
            sysDept.setTreePath(parentTreePath + "," + parentDept.getId());
        }

        sysDeptService.save(sysDept);

        return ResponseResult.okResult();
    }

    //更新部门
    @Override
    @Transactional
    public ResponseResult updateDept(Long id, UpdateDeptDto updateDeptDto)
    {
        //检查部门是否存在
        SysDept oldDept = sysDeptService.getById(id);
        if (oldDept == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该部门不存在！");
        }

        //校验部门名称是否重复(排除自己)
        LambdaQueryWrapper<SysDept> nameQueryWrapper = new LambdaQueryWrapper<>();
        nameQueryWrapper.eq(SysDept::getName, updateDeptDto.getName())
                        .ne(SysDept::getId, id);
        long nameCount = sysDeptService.count(nameQueryWrapper);
        if (nameCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "部门名称已存在，请修改后重试！");
        }

        //校验部门编号是否重复(排除自己)
        LambdaQueryWrapper<SysDept> codeQueryWrapper = new LambdaQueryWrapper<>();
        codeQueryWrapper.eq(SysDept::getCode, updateDeptDto.getCode())
                        .ne(SysDept::getId, id);
        long codeCount = sysDeptService.count(codeQueryWrapper);
        if (codeCount > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "部门编号已存在，请修改后重试！");
        }

        //检查是否将部门状态从正常改为禁用
        if (SystemConstants.STATUS_NORMAL.equals(oldDept.getStatus())
                && !SystemConstants.STATUS_NORMAL.equals(updateDeptDto.getStatus())) {
            //统计该部门下的成员数量
            LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
            userQueryWrapper.eq(SysUser::getId, id);
            long userCount = sysUserService.count(userQueryWrapper);

            if (userCount > 0) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该部门下存在成员，不可禁用！");
            }
        }

        //获取 父节点ID路径
        Long parentId = updateDeptDto.getParentId();
        oldDept.setParentId(parentId);

        //顶级部门设置 treePath
        if (Objects.equals(parentId, 0L)) {
            oldDept.setTreePath(SystemConstants.PARENT_NODE_PATH);//如果当前部门是顶级部门直接赋值 0
        } else {
            SysDept parentDept = sysDeptService.getById(parentId);//如果不是顶级部门就先查父部门是否存在
            if (parentDept == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "上级部门不存在！");
            }
            String parentTreePath = parentDept.getTreePath();//再取父部门的路径再用","拼接出当前部门完整的层级路径
            if (parentTreePath == null || parentTreePath.isBlank()) {
                parentTreePath = SystemConstants.PARENT_NODE_PATH;
            }
            oldDept.setTreePath(parentTreePath + "," + parentDept.getId());
        }

        //更新部门信息
        SysDept sysDept = BeanCopyUtils.copyBean(updateDeptDto, SysDept.class);
        sysDept.setId(id);
        sysDeptService.updateById(sysDept);

        return ResponseResult.okResult();
    }

    //批量删除部门（逻辑删除）
    @Override
    @Transactional
    public ResponseResult deleteDept(Long[] ids)
    {
        //检查是否有选择到要删除的部门
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的部门");
        }

        //获取部门id列表
        List<Long> idList = List.of(ids);

        //检查选择的部门是否存在
        List<SysDept> deptsToDelete = sysDeptService.listByIds(idList);
        if (deptsToDelete.size() != ids.length){
            //如果有不存在的部门，就找出对应的部门id
            Set<Long> existIds = deptsToDelete.stream()
                    .map(SysDept::getId)
                    .collect(Collectors.toSet());

            List<Long> notExistIds = idList.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败！！原因：以下部门 id 不存在：" + notExistIds);
        }

        StringBuilder errorMsg = new StringBuilder();

        //收集所有部门（包含子部门）并检查是否存在成员
        for (Long deptId : idList) {
            List<Long> deptAndChildrenIds = selectDeptAndChildrenIds(deptId);
            for (Long currentDeptId : deptAndChildrenIds) {
                LambdaQueryWrapper<SysUser> userQueryWrapper = new LambdaQueryWrapper<>();
                userQueryWrapper.eq(SysUser::getDeptId, currentDeptId);

                long userCount = sysUserService.count(userQueryWrapper);

                if (userCount > 0) {
                    //如果该部门或其子部门下存在用户并生成错误信息
                    SysDept dept = sysDeptService.getById(currentDeptId);
                    String deptName = dept != null ? dept.getName() : "ID:" + currentDeptId;
                    if (!errorMsg.isEmpty()) {
                        errorMsg.append("; ");
                    }
                    errorMsg.append("【").append(deptName).append("】下存在 ").append(userCount).append(" 位成员");
                }
            }
        }

        //如果有部门中存在成员，返回所有错误信息
        if (!errorMsg.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, errorMsg + "，无法删除");
        }

        //根据部门id列表删除部门
        sysDeptService.removeByIds(idList);

        return ResponseResult.okResult();
    }

    //查询部门及其子部门id
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

    //获取当前用户拥有的数据权限范围
    private void applyDeptDataScopeFilter(LambdaQueryWrapper<SysDept> queryWrapper)
    {
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
                    //只能查看本人创建/拥有的数据
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
    }

    //创建DeptList的菜单树
    private List<DeptListVo> builderDeptListTree(List<DeptListVo> deptListVos, Long parentId)
    {
        return deptListVos.stream()
                .filter(deptListVo -> parentId.equals(deptListVo.getParentId()))
                .map(menu -> menu.setChildren(getChildrenByDeptList(menu, deptListVos)))
                .collect(Collectors.toList());
    }

    //获取传入参数的 子部门集合
    private List<DeptListVo> getChildrenByDeptList(DeptListVo deptListVo, List<DeptListVo> deptListVos)
    {
        return deptListVos.stream()
                .filter(child -> deptListVo.getId().equals(child.getParentId()))
                .map(child -> child.setChildren(getChildrenByDeptList(child, deptListVos)))
                .collect(Collectors.toList());
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

