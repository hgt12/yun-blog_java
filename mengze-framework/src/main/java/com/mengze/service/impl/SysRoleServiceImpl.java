package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.RoleListDto;
import com.mengze.domain.entity.Link;
import com.mengze.domain.vo.AdminRoleListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.mapper.SysRoleMapper;
import com.mengze.domain.entity.SysRole;
import com.mengze.service.SysRoleService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.BeanUtils;
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
}

