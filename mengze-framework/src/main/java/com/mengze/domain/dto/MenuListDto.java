package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 菜单树列表请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "菜单树列表请求对象")
public class MenuListDto {

    @ApiModelProperty(value = "关键字（用于模糊搜索菜单名字）", example = "系统管理")
    private String keywords;
}
