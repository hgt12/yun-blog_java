package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddRoleDto;
import com.mengze.domain.dto.RoleListDto;
import com.mengze.domain.dto.UpdateRoleDto;
import com.mengze.domain.entity.SysRoleDept;
import com.mengze.domain.entity.SysRoleMenu;
import com.mengze.domain.entity.SysUserRole;
import com.mengze.domain.vo.AdminRoleListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.RoleFormDetailVo;
import com.mengze.domain.vo.RoleOptionVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysRoleMapper;
import com.mengze.domain.entity.SysRole;
import com.mengze.mapper.SysUserRoleMapper;
import com.mengze.service.SysRoleDeptService;
import com.mengze.service.SysRoleMenuService;
import com.mengze.service.SysRoleService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色表(SysRole)表服务实现类
 *
 * @author makejava
 * @since 2026-03-27 19:27:14
 */
@Service("sysRoleService")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService
{

    @Autowired
    private SysRoleService sysRoleService;

    @Autowired
    private SysRoleMenuService sysRoleMenuService;

    @Autowired
    private SysRoleDeptService sysRoleDeptService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    //根据用户id查询角色信息
    @Override
    public List<String> selectRoleCodeByUserId(Long id)
    {
        //判断是否为管理员，如果是则返回集合中只需要有admin
        if(id == 1L){
            List<String> roleCode = new ArrayList<>();
            roleCode.add("admin");
            return roleCode;
        }
        //否则查询当前用户具有的角色信息
        return getBaseMapper().selectRoleCodeByUserId(id);
    }

    //获取角色列表
    @Override
    public ResponseResult<PageVo> getRoleList(Integer pageNum, Integer pageSize, RoleListDto roleListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        //根据关键字模糊查询
        if(StringUtils.hasText(roleListDto.getKeywords())){
            wrapper.like(SysRole::getName, roleListDto.getKeywords());
        }

        //根据状态进行筛选
        if(StringUtils.hasText(roleListDto.getStatus())){
            wrapper.eq(SysRole::getStatus, roleListDto.getStatus());
        }

        //先以排序序号升序排序和序号相同时按创建时间降序排序
        wrapper.orderByAsc(SysRole::getSort)
                .orderByDesc(SysRole::getCreateTime);

        //分页查询
        Page<SysRole> page = new Page<>(pageNum, pageSize);
        sysRoleService.page(page, wrapper);

        //封装成vo返回
        List<AdminRoleListVo> roleListVos = BeanCopyUtils.copyBeanList(page.getRecords(), AdminRoleListVo.class);
        PageVo pageVo = new PageVo(roleListVos, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }

    //获取角色下拉列表
    @Override
    public ResponseResult getRoleOptions()
    {
        //根据角色id查出所有角色选项
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();

        //查询粗状态正常的角色并倒序排序
        wrapper.eq(SysRole::getStatus, SystemConstants.STATUS_NORMAL)
               .orderByAsc(SysRole::getSort);

        //封装成list
        List<SysRole> roleList = sysRoleService.list(wrapper);

        //转换成vo
        List<RoleOptionVo> roleOptionVos = BeanCopyUtils.copyBeanList(roleList, RoleOptionVo.class);

        return ResponseResult.okResult(roleOptionVos);
    }

    //获取角色菜单ID集合
    @Override
    public ResponseResult<List<Long>> getRoleMenuList(Long roleId)
    {
        //获取角色id
        SysRole sysRole = sysRoleService.getById(roleId);
        if (sysRole == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该角色不存在！");
        }

        //构建查询条件
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();

        //根据角色id查出该角色有多少菜单（通过角色菜单联表）
        wrapper.eq(SysRoleMenu::getRoleId, roleId)
                .select(SysRoleMenu::getMenuId);

        List<SysRoleMenu> roleMenuList = sysRoleMenuService.list(wrapper);

        //封装成 菜单id列表
        List<Long> menuIds = roleMenuList.stream()
                .map(SysRoleMenu::getMenuId)
                .toList();

        return ResponseResult.okResult(menuIds);
    }

    //分配角色菜单（权限）
    @Override
    public ResponseResult allocationRoleMenu(Long roleId, Long[] menuIds)
    {
        //获取角色id
        SysRole sysRole = sysRoleService.getById(roleId);
        if (sysRole == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该角色不存在！");
        }

        //根据角色id查询出角色关联的菜单id 进行删除
        LambdaQueryWrapper<SysRoleMenu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleMenu::getRoleId, roleId);
        sysRoleMenuService.remove(wrapper);

        //如果前端没有传菜单id 说明就是清空该角色权限
        if (menuIds == null || menuIds.length == 0) {
            return ResponseResult.okResult();
        }

        //构建新的角色菜单关系
        List<SysRoleMenu> roleMenuList = new ArrayList<>();
        for (Long menuId : menuIds) {
            SysRoleMenu roleMenu = new SysRoleMenu();
            roleMenu.setRoleId(roleId);
            roleMenu.setMenuId(menuId);
            roleMenuList.add(roleMenu);
        }

        //保存新的角色菜单关系
        boolean saveBatch = sysRoleMenuService.saveBatch(roleMenuList);
        if (!saveBatch) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "分配角色菜单失败！");
        }
        return ResponseResult.okResult();
    }

    //获取角色表单数据
    @Override
    public ResponseResult<RoleFormDetailVo> getRoleFormDetail(Long id)
    {
        //根据角色id查询
        SysRole sysRole = sysRoleService.getById(id);
        if (sysRole == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该角色不存在！");
        }

        //根据角色id查部门id列表
        RoleFormDetailVo roleFormDetailVo = BeanCopyUtils.copyBean(sysRole, RoleFormDetailVo.class);
        LambdaQueryWrapper<SysRoleDept> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRoleDept::getRoleId, id)
                .select(SysRoleDept::getDeptId);
        List<SysRoleDept> roleDeptList = sysRoleDeptService.list(wrapper);
        List<Long> deptIds = roleDeptList.stream()
                .map(SysRoleDept::getDeptId)
                .toList();
        roleFormDetailVo.setDeptIds(deptIds);

        return ResponseResult.okResult(roleFormDetailVo);
    }

    //获取角色部门ID集合
    @Override
    public ResponseResult<List<Long>> getRoleDeptList(Long roleId)
    {
        //获取角色id
        SysRole sysRole = sysRoleService.getById(roleId);
        if (sysRole == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该角色不存在！");
        }

        //构建查询条件
        LambdaQueryWrapper<SysRoleDept> wrapper = new LambdaQueryWrapper<>();

        //根据角色id查出该角色有属于哪些部门（通过角色部门联表）
        wrapper.eq(SysRoleDept::getRoleId, roleId)
                .select(SysRoleDept::getDeptId);

        List<SysRoleDept> roleDeptList = sysRoleDeptService.list(wrapper);

        //封装成 菜单id列表
        List<Long> deptIds = roleDeptList.stream()
                .map(SysRoleDept::getDeptId)
                .toList();

        return ResponseResult.okResult(deptIds);
    }

    //新增角色
    @Override
    @Transactional
    public ResponseResult addRole(AddRoleDto addRoleDto)
    {
        //检查角色编码是否重复
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, addRoleDto.getRoleCode());
        SysRole existRole = sysRoleService.getOne(wrapper);
        if (existRole != null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "角色编码已存在！");
        }

        //如果是自定义部门数据则必须选择部门
        if (addRoleDto.getDataScope() != null
                && addRoleDto.getDataScope().equals(SystemConstants.CUSTOM_DEPT_DATA_SCOPE)
                && (addRoleDto.getDeptIds() == null || addRoleDto.getDeptIds().isEmpty())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "自定义数据权限时必须选择部门！");
        }

        //转换dto实体
        SysRole sysRole = BeanCopyUtils.copyBean(addRoleDto, SysRole.class);

        //保存到角色表
        boolean save = sysRoleService.save(sysRole);
        if (!save) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "新增角色失败！");
        }

        //如果是自定义的数据权限 则保存角色部门关联数据（条件：dataScope 不为空 且 dataScope=5 为“自定义部门权限” 且 deptIds 不为空 且 deptIds 里面至少有一个部门）
        if (addRoleDto.getDataScope() != null
                && addRoleDto.getDataScope().equals(SystemConstants.CUSTOM_DEPT_DATA_SCOPE)
                && addRoleDto.getDeptIds() != null
                && !addRoleDto.getDeptIds().isEmpty()) {

            List<SysRoleDept> roleDeptList = new ArrayList<>();
            for (Long deptId : addRoleDto.getDeptIds()) {
                SysRoleDept roleDept = new SysRoleDept();
                roleDept.setRoleId(sysRole.getId());
                roleDept.setDeptId(deptId);
                roleDeptList.add(roleDept);
            }

            //将自定义的角色部门关联数据保存到 sys_role_dept 表
            boolean saveDept = sysRoleDeptService.saveBatch(roleDeptList);
            if (!saveDept) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "新增角色部门关联失败！");
            }
        }

        return ResponseResult.okResult();
    }

    //编辑角色
    @Override
    public ResponseResult updateRole(Long id, UpdateRoleDto updateRoleDto)
    {
        //检查角色是否存在
        SysRole oldRole = sysRoleService.getById(id);
        if (oldRole == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该角色不存在！");
        }

        //检查角色编码是否重复（排除自己）
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleCode, updateRoleDto.getRoleCode())
               .ne(SysRole::getId, id);
        SysRole existRole = sysRoleService.getOne(wrapper);
        if (existRole != null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "角色编码已存在！");
        }

        //自定义部门权限时必须选择部门
        if (updateRoleDto.getDataScope() != null
                && updateRoleDto.getDataScope().equals(SystemConstants.CUSTOM_DEPT_DATA_SCOPE)
                && (updateRoleDto.getDeptIds() == null || updateRoleDto.getDeptIds().isEmpty())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "自定义数据权限时必须选择部门！");
        }

        //更新角色表
        SysRole sysRole = BeanCopyUtils.copyBean(updateRoleDto, SysRole.class);
        sysRole.setId(id);
        boolean update = sysRoleService.updateById(sysRole);
        if (!update) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "编辑角色失败！");
        }

        //先删除旧的角色部门关联
        LambdaQueryWrapper<SysRoleDept> deptWrapper = new LambdaQueryWrapper<>();
        deptWrapper.eq(SysRoleDept::getRoleId, id);
        sysRoleDeptService.remove(deptWrapper);

        //如果是自定义部门权限 则保存新的关联关系
        if (updateRoleDto.getDataScope() != null
                && updateRoleDto.getDataScope().equals(SystemConstants.CUSTOM_DEPT_DATA_SCOPE)
                && updateRoleDto.getDeptIds() != null
                && !updateRoleDto.getDeptIds().isEmpty()) {
            List<SysRoleDept> roleDeptList = new ArrayList<>();
            for (Long deptId : updateRoleDto.getDeptIds()) {
                SysRoleDept roleDept = new SysRoleDept();
                roleDept.setRoleId(id);
                roleDept.setDeptId(deptId);
                roleDeptList.add(roleDept);
            }

            //将自定义的角色部门关联数据保存到 sys_role_dept 表
            boolean saveDept = sysRoleDeptService.saveBatch(roleDeptList);
            if (!saveDept) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "编辑角色部门关联失败！");
            }
        }

        return ResponseResult.okResult();
    }

    //删除角色（包含批量，逻辑删除）
    @Override
    @Transactional
    public ResponseResult deleteRole(Long[] ids)
    {
        // 参数校验
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的角色");
        }

        List<Long> idList = List.of(ids);

        //检查角色是否存在
        List<SysRole> rolesToDelete = sysRoleService.listByIds(idList);
        if (rolesToDelete.size() != ids.length) {
            //找出不存在的角色id
            Set<Long> existIds = rolesToDelete.stream()
                    .map(SysRole::getId)
                    .collect(Collectors.toSet());

            List<Long> notExistIds = idList.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败！！原因：以下角色 id 不存在：" + notExistIds);
        }

        //禁止删除系统内置角色
        List<String> protectedRoleCodes = List.of(SystemConstants.ROLE_IS_ADMIN);
        List<Long> protectedRoleIds = rolesToDelete.stream()
                .filter(role -> protectedRoleCodes.contains(role.getRoleCode()) || Objects.equals(role.getId(), 1L))
                .map(SysRole::getId)
                .toList();
        if (!protectedRoleIds.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败！！原因：系统内置角色不允许删除：" + protectedRoleIds);
        }

        //检查该角色是否有用户存在 有则不允许删除
        LambdaQueryWrapper<SysUserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.in(SysUserRole::getRoleId, idList)
                .select(SysUserRole::getRoleId);
        List<SysUserRole> userRoleList = sysUserRoleMapper.selectList(userRoleWrapper);
        if (!userRoleList.isEmpty()) {
            List<Long> assignedRoleIds = userRoleList.stream()
                    .map(SysUserRole::getRoleId)
                    .distinct()
                    .toList();
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败！！原因：以下角色已分配给用户，不能删除：" + assignedRoleIds);
        }

        //删除角色（逻辑删除）
        //注意：不应该将角色和菜单、部门的关联关系也一并删除，避免后续恢复角色时丢失权限配置
        boolean remove = sysRoleService.removeByIds(idList);
        if (!remove) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除角色失败！");
        }

        return ResponseResult.okResult();
    }
}

