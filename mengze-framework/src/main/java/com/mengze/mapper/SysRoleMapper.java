package com.mengze.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mengze.domain.entity.SysRole;

import java.util.List;

/**
 * 角色表(SysRole)表数据库访问层
 *
 * @author makejava
 * @since 2026-03-27 19:27:14
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    List<String> selectRoleCodeByUserId(Long id);
}

