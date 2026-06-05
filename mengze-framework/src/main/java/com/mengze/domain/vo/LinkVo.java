package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "友链响应对象")
public class LinkVo
{
    @ApiModelProperty(value = "友链ID", example = "1")
    private Long id;
    
    @ApiModelProperty(value = "友链名字", example = "技术博客")
    private String name;

    @ApiModelProperty(value = "友链状态", example = "0")
    private String status;
    
    @ApiModelProperty(value = "友链Logo", example = "https://example.com/logo.png")
    private String logo;
    
    @ApiModelProperty(value = "个人描述", example = "一个专注于技术分享的博客")
    private String description;
    
    @ApiModelProperty(value = "网站地址", example = "https://example.com")
    private String address;
}
