package com.mengze.domain.dto;

import lombok.Data;

//发送验证码请求DTO
@Data
public class SendCodeDto {
    
    /**
     * 账号（邮箱或手机号，后端自动识别）
     */
    private String account;
    
    /**
     * 验证码类型：LOGIN-登录, REGISTER-注册, FORGOT_PASSWORD-忘记密码
     */
    private String type;
}
