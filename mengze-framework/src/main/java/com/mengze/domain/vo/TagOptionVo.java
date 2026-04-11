package com.mengze.domain.vo;

import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "标签选项")
public class TagOptionVo {
    @ApiModelProperty(value = "标签ID")
    @JSONField(name = "value")//转换成 value 再响应给前端
    private Long id;

    @ApiModelProperty(value = "标签名称")
    @JSONField(name = "label")//转换成 label 再响应给前端
    private String name;
}
