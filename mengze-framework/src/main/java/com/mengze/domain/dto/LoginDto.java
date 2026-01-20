package com.mengze.domain.dto;

import lombok.Data;

//登录请求DTO
@Data
public class LoginDto {
    
    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 验证码
     */
    private String code;
}
