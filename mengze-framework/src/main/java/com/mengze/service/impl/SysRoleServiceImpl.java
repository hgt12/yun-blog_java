package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.RoleListDto;
import com.mengze.domain.entity.SysRoleDept;
import com.mengze.domain.entity.SysRoleMenu;
import com.mengze.domain.vo.AdminRoleListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.RoleFormDetailVo;
import com.mengze.domain.vo.RoleOptionVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysRoleMapper;
import com.mengze.domain.entity.SysRole;
import com.mengze.mapper.SysRoleMenuMapper;
import com.mengze.service.SysRoleDeptService;
import com.mengze.service.SysRoleMenuService;
import com.mengze.service.SysRoleService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

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
}

