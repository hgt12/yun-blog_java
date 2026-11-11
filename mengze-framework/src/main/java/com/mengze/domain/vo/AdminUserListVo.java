package com.mengze.domain.vo;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户列表Vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "后台用户列表响应对象")
public class AdminUserListVo
{

    @ApiModelProperty(value = "用户id", example = "1")
    private Long id;

    @ApiModelProperty(value = "头像URL", example = "https://avatars.githubusercontent.com/u/155459505?v=4")
    private String avatar;

    @ApiModelProperty(value = "创建时间", example = "2026-01-12 20:58:53")
    private Date createTime;

    @ApiModelProperty(value = "部门名称", example = "测试不")
    private String deptName;

    @ApiModelProperty(value = "用户邮箱", example = "admin@example.com")
    private String email;

    @ApiModelProperty(value = "用户性别，（0 男，1 女，2 隐藏）", example = "0")
    private String sex;

    @ApiModelProperty(value = "手机号", example = "13800138000")
    @JSONField(name = "mobile")//转换成 mobile 再响应给前端
    private String phone;

    @ApiModelProperty(value = "昵称", example = "云梦泽")
    private String nickname;

    @ApiModelProperty(value = "角色名称，多个使用英文逗号(,)分割", example = "系统管理员")
    private String roleNames;

    @ApiModelProperty(value = "账号状态（0 正常，1 停用）", example = "0")
    private String status;

    @ApiModelProperty(value = "用户名", example = "云梦泽")
    private String username;
}
