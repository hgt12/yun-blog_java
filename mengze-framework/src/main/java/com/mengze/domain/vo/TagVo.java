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
public class TagVo
{
    @ApiModelProperty(value = "标签ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "标签名", example = "Java")
    private String name;

    @ApiModelProperty(value = "标签状态", example = "0")
    private String status;

    @ApiModelProperty(value = "文章数量", example = "10")
    private Integer articleCount;

    @ApiModelProperty(value = "创建日期", example = "2024-01-01 12:00:00")
    private Date createTime;
}
