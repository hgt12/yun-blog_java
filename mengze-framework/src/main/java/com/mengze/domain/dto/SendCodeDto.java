package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//发送验证码请求DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "发送验证码请求对象")
public class SendCodeDto {
    
    @ApiModelProperty(value = "账号（邮箱或手机号，后端自动识别）", required = true, example = "user@example.com")
    private String account;
    
    @ApiModelProperty(value = "验证码类型：LOGIN-登录, REGISTER-注册, FORGOT_PASSWORD-忘记密码", required = true, example = "LOGIN")
    private String type;
}
