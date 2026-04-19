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
@ApiModel(description = "标签响应对象")
public class AdminTagListVo
{
    @ApiModelProperty(value = "标签ID")
    private Long id;

    @ApiModelProperty(value = "标签名")
    private String name;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）")
    private String status;

    @ApiModelProperty(value = "排序")
    private Integer sort;

    @ApiModelProperty(value = "备注")
    private String remark;

    @ApiModelProperty(value = "文章数量")
    private Integer articleCount;

    @ApiModelProperty(value = "创建日期")
    private Date createTime;
}
