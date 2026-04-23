package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新友链请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "更新友链请求对象")
public class UpdateLinkDto {

    @ApiModelProperty(value = "友链ID", required = true, example = "1")
    private Long id;

    @ApiModelProperty(value = "站点名称", example = "云梦泽的个人博客")
    private String name;
    
    @ApiModelProperty(value = "个人描述", example = "这里是云梦泽的个人博客")
    private String description;

    @ApiModelProperty(value = "站点地址", required = true, example = "https://hgt12.github.io")
    private String address;

    @ApiModelProperty(value = "头像地址", example = "https://hgt12.github.io/img/mi_manchi.png")
    private String logo;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;
}
