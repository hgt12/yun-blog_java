package com.mengze.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据字典类型表(SysDict)表实体类
 *
 * @author makejava
 * @since 2026-05-24 14:07:03
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_dict")
public class SysDict extends Model<SysDict> {
    @TableId
    //主键 
    private Long id;
    //类型编码
    private String dictCode;
    //类型名称
    private String name;
    //状态(0:正常;1:禁用)
    private String status;
    //备注
    private String remark;
    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    //创建人ID
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
    //修改人ID
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    //是否删除(0-未删除, 1-删除)
    private String delFlag;

}

