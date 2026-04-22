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
    @ApiModelProperty(value = "友链ID")
    private Long id;

    @ApiModelProperty(value = "站点名称")
    private String name;

    @ApiModelProperty(value = "头像链接")
    private String logo;

    @ApiModelProperty(value = "网站地址")
    private String address;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）")
    private String status;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "个人描述")
    private String description;

    @ApiModelProperty(value = "创建日期")
    private Date createTime;
}
