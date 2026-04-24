package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

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

    @NotBlank(message = "站点名称不能为空")
    @ApiModelProperty(value = "站点名称", example = "云梦泽的个人博客")
    private String name;
    
    @ApiModelProperty(value = "个人描述", example = "这里是云梦泽的个人博客")
    private String description;

    @NotBlank(message = "站点地址不能为空")
    @Pattern(regexp = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "站点地址格式不正确，请输入有效的URL")
    @ApiModelProperty(value = "站点地址", required = true, example = "https://hgt12.github.io")
    private String address;

    @Pattern(regexp = "^$|^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "头像地址格式不正确，请输入有效的URL")
    @ApiModelProperty(value = "头像地址", example = "https://hgt12.github.io/img/mi_manchi.png")
    private String logo;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @Pattern(regexp = "^$|^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;
}
