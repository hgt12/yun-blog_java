package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddUserDto;
import com.mengze.domain.dto.UpdateUserDto;
import com.mengze.domain.dto.UserListDto;
import com.mengze.domain.entity.SysUser;
import com.mengze.domain.vo.UserFormDetailVo;
import javax.servlet.http.HttpServletResponse;

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

    ResponseResult getUserList(Integer pageNum, Integer pageSize, UserListDto userListDto);

    ResponseResult<UserFormDetailVo> getUserFormDetail(Long userId);

    ResponseResult addUser(AddUserDto addUserDto);

    ResponseResult updateUser(Long id, UpdateUserDto updateUserDto);

    ResponseResult deleteUser(Long[] ids);

    ResponseResult updateResetUserPassword(Long id, String password);

    void downloadImportUsersTemplate(HttpServletResponse response);
}

