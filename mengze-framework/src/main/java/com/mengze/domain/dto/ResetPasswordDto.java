package com.mengze.domain.dto;

import lombok.Data;

/**
 * 重置密码DTO
 */
@Data
public class ResetPasswordDto {
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 验证码
     */
    private String code;
    
    /**
     * 新密码
     */
    private String newPassword;
}
