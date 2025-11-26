package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类表(Category)表实体类
 *
 * @author makejava
 * @since 2025-11-26 16:28:18
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("category")
public class Category extends Model<Category> {
    @TableId

    private Long id;
    //分类名
    private String name;
    //预留字段，父分类id，如果没有父分类为-1
    private Long pid;
    //描述
    private String description;
    //状态0:正常,1禁用
    private String status;
    //创建者
    private Long createBy;
    //创建时间
    private Date createTime;
    //修改者
    private Long updateBy;

    private Date updateTime;
    //删除标志（0代表未删除，1代表已删除）
    private Integer delFlag;

}

