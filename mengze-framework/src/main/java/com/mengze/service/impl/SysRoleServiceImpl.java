package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.mapper.SysRoleMapper;
import com.mengze.domain.entity.SysRole;
import com.mengze.service.SysRoleService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 角色表(SysRole)表服务实现类
 *
 * @author makejava
 * @since 2026-03-27 19:27:14
 */
@Service("sysRoleService")
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

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
}

