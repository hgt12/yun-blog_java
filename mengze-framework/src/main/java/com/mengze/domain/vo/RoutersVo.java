package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 获取当前登录用户可访问的前端路由树响应包装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "获取当前登录用户可访问的前端路由树响应对象")
public class RoutersVo
{
    private List<MenuVo> menus;
}
