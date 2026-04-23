package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加友链请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "添加友链请求对象")
public class AddLinkDto {
    
    @ApiModelProperty(value = "站点名称", required = true, example = "站点名")
    private String name;
    
    @ApiModelProperty(value = "站点地址", required = true, example = "https://hgt12.github.io")
    private String address;

    @ApiModelProperty(value = "头像地址", example = "https://hgt12.github.io/img/mi_manchi.png")
    private String logo;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", required = true, example = "0")
    private String status;

    @ApiModelProperty(value = "个人描述", example = "我是云梦泽")
    private String description;

    @ApiModelProperty(value = "排序", example = "0")
    private Number sort;
}
