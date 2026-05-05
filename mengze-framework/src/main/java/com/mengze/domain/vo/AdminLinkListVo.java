package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "友链响应对象")
public class AdminLinkListVo
{
    @ApiModelProperty(value = "友链ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "站点名称", example = "技术博客")
    private String name;

    @ApiModelProperty(value = "头像链接", example = "https://example.com/logo.png")
    private String logo;

    @ApiModelProperty(value = "网站地址", example = "https://example.com")
    private String address;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "个人描述", example = "一个专注于技术分享的博客")
    private String description;

    @ApiModelProperty(value = "创建日期", example = "2024-01-01 12:00:00")
    private Date createTime;
}
