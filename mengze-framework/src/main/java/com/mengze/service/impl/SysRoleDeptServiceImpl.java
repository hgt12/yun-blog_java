package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.mapper.SysRoleDeptMapper;
import com.mengze.domain.entity.SysRoleDept;
import com.mengze.service.SysRoleDeptService;
import org.springframework.stereotype.Service;

/**
 * 角色部门数据权限关联表(SysRoleDept)表服务实现类
 *
 * @author makejava
 * @since 2026-05-15 13:14:58
 */
@Service("sysRoleDeptService")
public class SysRoleDeptServiceImpl extends ServiceImpl<SysRoleDeptMapper, SysRoleDept> implements SysRoleDeptService {

}

