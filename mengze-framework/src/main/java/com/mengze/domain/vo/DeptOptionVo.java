package com.mengze.domain.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "部门选项")
public class DeptOptionVo {
    @ApiModelProperty(value = "部门ID", example = "1")
    @JSONField(name = "value")//转换成 value 再响应给前端
    private Long id;

    @ApiModelProperty(value = "父部门ID", example = "0")
    private Long parentId;

    @ApiModelProperty(value = "部门名称", example = "总公司")
    @JSONField(name = "label")//转换成 label 再响应给前端
    private String name;

    @TableField(exist = false)
    @ApiModelProperty(value = "子部门列表")
    private List<DeptOptionVo> children;
}
