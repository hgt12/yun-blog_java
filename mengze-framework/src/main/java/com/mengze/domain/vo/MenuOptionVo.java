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
@ApiModel(description = "菜单选项")
public class MenuOptionVo
{
    @ApiModelProperty(value = "菜单ID", example = "1")
    @JSONField(name = "value")//转换成 value 再响应给前端
    private Long id;

    @ApiModelProperty(value = "菜单名称", example = "系统管理")
    @JSONField(name = "label")//转换成 label 再响应给前端
    private String name;

    @ApiModelProperty(value = "父菜单ID", hidden = true, example = "0")
    @JSONField(serialize = false)//不序列化到JSON响应中
    private Long parentId;

    @TableField(exist = false)
    @ApiModelProperty(value = "子菜单")
    private List<MenuOptionVo> children;

}
