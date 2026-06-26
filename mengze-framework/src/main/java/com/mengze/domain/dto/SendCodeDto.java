package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

//发送验证码请求DTO
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "发送验证码请求对象")
public class SendCodeDto
{

    @NotBlank(message = "账号不能为空")
    @ApiModelProperty(value = "账号（邮箱或手机号，后端自动识别）", required = true, example = "user@example.com")
    private String account;

    @NotBlank(message = "验证码类型不能为空")
    @Pattern(regexp = "^(LOGIN|REGISTER|FORGOT_PASSWORD)$", message = "验证码类型只能为LOGIN、REGISTER或FORGOT_PASSWORD")
    @ApiModelProperty(value = "验证码类型：LOGIN-登录, REGISTER-注册, FORGOT_PASSWORD-忘记密码", required = true, example = "LOGIN")
    private String type;

    @ApiModelProperty(value = "图形验证码二次验证Token（通过/genCaptcha获取验证码并完成校验后获得）", example = "a1b2c3d4e5f6")
    private String captchaToken;
}
