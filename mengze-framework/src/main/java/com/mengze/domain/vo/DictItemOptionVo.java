package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "字典选项")
public class DictItemOptionVo {
    @ApiModelProperty(value = "字典项值", example = "1")
    private String value;

    @ApiModelProperty(value = "字典项标签", example = "男")
    private String label;

    @ApiModelProperty(value = "标签类型，用于前端样式展示（如success、warning等）", example = "success")
    private String tagType;
}
