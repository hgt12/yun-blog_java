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
@ApiModel(description = "角色选项响应对象")
public class RoleOptionVo {
    @ApiModelProperty(value = "角色ID", example = "3")
    @JSONField(name = "value")//转换成 value 再响应给前端
    private Long id;

    @ApiModelProperty(value = "角色名称", example = "游客")
    @JSONField(name = "label")//转换成 label 再响应给前端
    private String name;
}
