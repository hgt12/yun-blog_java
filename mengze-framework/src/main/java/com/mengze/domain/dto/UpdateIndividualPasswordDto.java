package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 更新个人密码请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "更新个人密码请求对象")
public class UpdateIndividualPasswordDto
{
    @NotBlank(message = "旧密码不能为空")
    @ApiModelProperty(value = "旧密码", required = true, example = "oldPassword123456")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&.]{8,20}$",
            message = "密码必须包含大小写字母和数字，长度8-20位")
    @ApiModelProperty(value = "新密码", required = true, example = "newPassword123456")
    private String newPassword;

    @NotBlank(message = "确认密码不能为空")
    @ApiModelProperty(value = "确认密码", required = true, example = "newPassword123456")
    private String confirmPassword;

}
