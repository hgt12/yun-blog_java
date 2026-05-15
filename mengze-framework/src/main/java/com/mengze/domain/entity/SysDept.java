package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门表(SysDept)表实体类
 *
 * @author makejava
 * @since 2026-05-15 13:05:02
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_dept")
public class SysDept extends Model<SysDept> {
    @TableId

    private Long id;
    //部门名称
    private String name;
    //部门编号
    private String code;
    //父部门ID
    private Long parentId;
    //排序
    private Integer sort;
    //状态(0:启用;1:禁用)
    private String status;
    //父节点ID路径
    private String treePath;
    //删除标记(0-未删除,1-已删除)
    private String delFlag;
    //创建者ID
    private Long createBy;
    //创建时间
    private Date createTime;
    //更新者ID
    private Long updateBy;
    //更新时间
    private Date updateTime;

}

