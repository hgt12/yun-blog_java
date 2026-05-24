package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "字典项响应对象")
public class DictItemListVo
{
    @ApiModelProperty(value = "字典项ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "字典项标签", example = "男")
    private String label;

    @ApiModelProperty(value = "关联字典编码，与sys_dict表中的dict_code对应", example = "sex")
    private String dictCode;

    @ApiModelProperty(value = "字典项值", example = "1")
    private String value;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;

    @ApiModelProperty(value = "排序", example = "1")
    private Integer sort;

    @ApiModelProperty(value = "标签类型，用于前端样式展示（如success、warning等）", example = "success")
    private String tagType;
}
