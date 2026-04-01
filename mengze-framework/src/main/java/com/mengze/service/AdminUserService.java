package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.SysUser;

/**
 * 用户表(SysUser)表服务接口(后台)
 *
 * @author makejava
 * @since 2026-01-14 15:21:53
 */
public interface AdminUserService extends IService<SysUser> {

    /**
     * 获取用户信息（包含权限和角色）
     */
    ResponseResult getAdminInfo();
}

