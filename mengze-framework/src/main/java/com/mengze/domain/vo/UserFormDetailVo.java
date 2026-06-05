package com.mengze.domain.vo;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "后台用户表单详情响应对象")
public class UserFormDetailVo
{
    @ApiModelProperty(value = "用户id", example = "1")
    private Long id;

    @ApiModelProperty(value = "头像URL", example = "https://avatars.githubusercontent.com/u/155459505?v=4")
    private String avatar;

    @ApiModelProperty(value = "部门id", example = "测试不")
    private String deptId;

    @ApiModelProperty(value = "用户邮箱", example = "2962933152@qq.com")
    private String email;

    @ApiModelProperty(value = "用户性别，（0 男，1 女，2 隐藏）", example = "0")
    private String sex;

    @ApiModelProperty(value = "手机号", example = "18329384753")
    @JSONField(name = "mobile")//转换成 mobile 再响应给前端
    private String phone;

    @ApiModelProperty(value = "昵称", example = "云梦泽")
    private String nickname;

    @ApiModelProperty(value = "角色id列表", example = "[1,2,3]")
    private List<Long> roleIds;

    @ApiModelProperty(value = "账号状态（0 正常，1 停用）", example = "0")
    private String status;

    @ApiModelProperty(value = "用户名", example = "云梦泽")
    private String username;
}
