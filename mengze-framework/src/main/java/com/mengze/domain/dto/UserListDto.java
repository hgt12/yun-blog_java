package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 用户列表请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户列表请求对象")
public class UserListDto {

    @Pattern(regexp = "^$|^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "状态", example = "0")
    private String status;

    @ApiModelProperty(value = "关键字（用户名/昵称/手机号）", example = "云梦泽")
    private String keywords;

    @ApiModelProperty(value = "创建时间", example = "[2026-04-01, 2026-05-15]")
    private List<String> createTime;

    @ApiModelProperty(value = "部门id", example = "1")
    private String deptId;
}
