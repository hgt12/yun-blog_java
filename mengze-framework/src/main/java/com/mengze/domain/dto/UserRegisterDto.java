package com.mengze.domain.dto;

import lombok.Data;

//用户注册DTO
@Data
public class UserRegisterDto {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 邮箱（邮箱注册时必填）
     */
    private String email;
    
    /**
     * 手机号（手机注册时必填）
     */
    private String phone;
    
    /**
     * 验证码（必填）
     */
    private String code;

    /**
     * 用户名代替昵称
     */
    private String nickname;

}
