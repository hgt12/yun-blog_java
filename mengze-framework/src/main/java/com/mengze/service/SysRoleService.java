package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.RoleListDto;
import com.mengze.domain.entity.SysRole;
import com.mengze.domain.vo.PageVo;

import java.util.List;

/**
 * 角色表(SysRole)表服务接口
 *
 * @author makejava
 * @since 2026-03-27 19:27:14
 */
public interface SysRoleService extends IService<SysRole> {

    List<String> selectRoleCodeByUserId(Long id);

    ResponseResult<PageVo> getRoleList(Integer pageNum, Integer pageSize, RoleListDto roleListDto);

    ResponseResult getRoleOptions();

    ResponseResult<List<Long>> getRoleMenuList(Long roleId);
}

