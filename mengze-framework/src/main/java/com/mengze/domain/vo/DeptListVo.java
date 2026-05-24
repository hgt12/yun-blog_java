package com.mengze.domain.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 部门Vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "获取当前登录用户所属部门树列表")
public class DeptListVo
{
    @ApiModelProperty(value = "部门id", example = "6")
    private Long id;

    @ApiModelProperty(value = "父ID", example = "1")
    private Long parentId;

    @ApiModelProperty(value = "部门名称", example = "总部")
    private String name;

    @ApiModelProperty(value = "部门编号", example = "ROOT")
    private String code;

    @ApiModelProperty(value = "排序", example = "5")
    private Integer sort;

    @ApiModelProperty(value = "状态（0-启用，1-禁用）", example = "0")
    private String status;

    @ApiModelProperty(value = "父节点ID路径", example = "1,2")
    private String treePath;

    @ApiModelProperty(value = "创建时间", example = "2024-01-01 12:00:00")
    private Date createTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "子部门")
    private List<DeptListVo> children;
}
