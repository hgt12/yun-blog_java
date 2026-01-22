package com.mengze.domain.dto;

import lombok.Data;

/**
 * 验证重置验证码DTO
 */
@Data
public class VerifyResetCodeDto {
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 验证码
     */
    private String code;
}
