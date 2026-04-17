package com.mengze.domain.vo;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "分类选项")
public class CategoryOptionVo {
    @ApiModelProperty(value = "分类ID")
    @JSONField(name = "value")//转换成 value 再响应给前端
    private Long id;

    @ApiModelProperty(value = "分类名称")
    @JSONField(name = "label")//转换成 label 再响应给前端
    private String name;
}
