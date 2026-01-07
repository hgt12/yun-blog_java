package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 分类表(Category)表实体类
 *
 * @author makejava
 * @since 2026-01-07 13:23:58
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("category")
public class Category extends Model<Category> {
    @TableId
    //分类ID
    private Long id;
    //分类名称
    private String name;
    //分类描述
    private String description;
    //父分类ID（0表示顶级分类）
    private Long pid;
    //排序
    private Integer sort;
    //状态（0-正常，1-禁用）
    private String status;
    //删除标志（0-未删除，1-已删除）
    private String delFlag;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;

}

