package com.mengze.utils;

import com.mengze.domain.entity.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全工具类 - 获取当前登录用户信息
 */
public class SecurityUtils
{

    /**
     * 获取当前登录用户
     * @return LoginUser 或 null（未登录）
     */
    public static LoginUser getLoginUser()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        return null;
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID 或 null
     */
    public static Long getUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getSysUser().getId() : null;
    }

    /**
     * 判断当前用户是否已登录
     * @return true-已登录，false-未登录
     */
    public static boolean isLogin() {
        return getLoginUser() != null;
    }

    /**
     *  用户是管理员
     */
    public static boolean isAdmin() {
        Long userId = getLoginUser().getSysUser().getId();
        return userId != null && userId.equals(1L);
    }
}
