package com.mengze.domain.dto;

import lombok.Data;

//发送验证码请求DTO
@Data
public class SendCodeDto {
    
    /**
     * 邮箱地址（邮箱验证码时使用）
     */
    private String email;
    
    /**
     * 手机号（手机验证码时使用）
     */
    private String phone;
}
