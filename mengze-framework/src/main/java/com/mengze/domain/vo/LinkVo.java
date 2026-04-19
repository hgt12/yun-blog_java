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
public class LinkVo
{
    @ApiModelProperty(value = "友链ID")
    private Long id;
    
    @ApiModelProperty(value = "友链名字")
    private String name;

    @ApiModelProperty(value = "友链状态")
    private String status;
    
    @ApiModelProperty(value = "友链Logo")
    private String logo;
    
    @ApiModelProperty(value = "个人描述")
    private String description;
    
    @ApiModelProperty(value = "网站地址")
    private String address;
}
