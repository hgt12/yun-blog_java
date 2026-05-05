package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 更新菜单请求DTO
 * 
 * @author mengze
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "更新菜单请求对象")
public class UpdateMenuDto {

    @ApiModelProperty(value = "菜单ID", required = true, example = "1")
    private Long id;

    @NotNull(message = "父菜单ID不能为空")
    @ApiModelProperty(value = "父菜单ID（0表示顶级菜单）", required = true, example = "0")
    private Long parentId;

    @NotBlank(message = "菜单名称不能为空")
    @ApiModelProperty(value = "菜单名称", required = true, example = "博客管理")
    private String name;

    @NotBlank(message = "菜单类型不能为空")
    @Pattern(regexp = "^[CMB]$", message = "菜单类型只能为C（目录）、M（菜单）或B（按钮）")
    @ApiModelProperty(value = "菜单类型（C-目录 M-菜单 B-按钮）", required = true, example = "C")
    private String type;
    
    @ApiModelProperty(value = "路由路径", example = "/blog")
    private String path;

    @ApiModelProperty(value = "路由名称（前端路由name）", example = "Blog")
    private String routeName;

    @ApiModelProperty(value = "路由路径（自定义路由路径（菜单和目录必填，按钮可不填））", example = "/blog/list")
    private String routePath;

    @ApiModelProperty(value = "重定向路径", example = "/blog/list")
    private String redirect;

    @ApiModelProperty(value = "组件路径（目录填Layout，菜单填组件路径）", example = "blog/index")
    private String component;
    
    @ApiModelProperty(value = "菜单图标", example = "el-icon-document")
    private String icon;

    @ApiModelProperty(value = "显示排序（数字越小越靠前）", example = "1")
    private Integer sort;

    @Pattern(regexp = "^[01]$", message = "菜单可见状态只能为0或1")
    @ApiModelProperty(value = "菜单是否可见（0-显示 1-隐藏）",example = "0")
    private String visible;

    @ApiModelProperty(value = "目录是否始终显示（0-显示 1-隐藏）",example = "0")
    private String alwaysShow;
    
    @ApiModelProperty(value = "权限标识（按钮必填）", example = "system:user:add")
    private String perm;

    @ApiModelProperty(value = "是否缓存（0-缓存 1-不缓存）", example = "0")
    private String keepAlive;

    @ApiModelProperty(value = "路由参数数组", example = "[{\"key\":\"from\",\"value\":\"admin\"}]")
    private List<MenuParamDto> params;
}
