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
@ApiModel(description = "用户下拉选项")
public class UserOptionVo {
    @ApiModelProperty(value = "用户ID", example = "1")
    @JSONField(name = "value")//转换成 value 再响应给前端
    private Long id;

    @ApiModelProperty(value = "用户名称", example = "云梦泽")
    @JSONField(name = "label")//转换成 label 再响应给前端
    private String username;
}
