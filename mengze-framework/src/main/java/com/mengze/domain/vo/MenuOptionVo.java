package com.mengze.domain.vo;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "菜单选项")
public class MenuOptionVo
{
    @ApiModelProperty(value = "菜单ID")
    @JSONField(name = "value")//转换成 value 再响应给前端
    private Long id;

    @ApiModelProperty(value = "菜单名称")
    @JSONField(name = "label")//转换成 label 再响应给前端
    private String name;

    @ApiModelProperty(value = "父菜单ID", hidden = true)
    @JSONField(serialize = false)//不序列化到JSON响应中
    private Long parentId;

    @TableField(exist = false)
    private List<MenuOptionVo> children;

    //方便链式调用
    public MenuOptionVo setChildren(List<MenuOptionVo> children) {
        this.children = children;
        return this;
    }
}
