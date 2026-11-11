package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 添加用户请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "添加用户请求对象")
public class AddUserDto {

    @ApiModelProperty(value = "用户头像url", example = "https://avatars.githubusercontent.com/u/155459505?v=4")
    private String avatar;

    @NotBlank(message = "部门id不能为空")
    @ApiModelProperty(value = "部门id", required = true, example = "3")
    private String deptId;

    @ApiModelProperty(value = "邮箱", example = "admin@example.com")
    private String email;

    @ApiModelProperty(value = "用户性别，（0 男，1 女，2 隐藏）", example = "0")
    private String sex;

    @ApiModelProperty(value = "手机号", example = "1234567890")
    private String mobile;

    @NotBlank(message = "昵称不能为空")
    @ApiModelProperty(value = "昵称", required = true, example = "云梦泽")
    private String nickname;

    @NotEmpty(message = "角色不能为空")
    @ApiModelProperty(value = "角色id列表", required = true, example = "[1,2,3]")
    private List<Long> roleIds;

    @NotBlank(message = "状态不能为空")
    @Pattern(regexp = "^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "状态（0-正常，1-禁用）", required = true, example = "0")
    private String status;

    @NotBlank(message = "用户名不能为空")
    @ApiModelProperty(value = "用户名", required = true, example = "云梦泽")
    private String username;

}
