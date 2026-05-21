package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 修改个人中心信息请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "修改个人中心信息请求对象")
public class UpdateUserProfileDto
{
    @NotBlank(message = "昵称不能为空")
    @ApiModelProperty(value = "昵称", required = true, example = "云梦泽")
    private String nickname;

    @ApiModelProperty(value = "用户头像url", example = "https://avatars.githubusercontent.com/u/155459505?v=4")
    private String avatar;

    @ApiModelProperty(value = "用户性别，（0 男，1 女，2 隐藏）", example = "0")
    private String sex;
}
