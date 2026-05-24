package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "字典表单详情响应对象")
public class DictFormDetailVo
{
    @ApiModelProperty(value = "字典ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "字典名", example = "性别")
    private String name;

    @ApiModelProperty(value = "字典编码", example = "sex")
    private String dictCode;

    @ApiModelProperty(value = "状态（0-正常，1-禁用）", example = "0")
    private String status;

    @ApiModelProperty(value = "备注", example = "这里是字典备注")
    private String remark;
}
