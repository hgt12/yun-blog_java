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
    @ApiModelProperty(value = "标签ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "标签名", example = "Java")
    private String name;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "备注", example = "Java相关技术文章")
    private String remark;

    @ApiModelProperty(value = "文章数量", example = "10")
    private Integer articleCount;

    @ApiModelProperty(value = "创建日期", example = "2024-01-01 12:00:00")
    private Date createTime;
}
