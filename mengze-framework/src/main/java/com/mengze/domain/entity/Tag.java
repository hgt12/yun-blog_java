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
 * 标签表(Tag)表实体类
 *
 * @author makejava
 * @since 2026-01-11 18:59:54
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tag")
public class Tag extends Model<Tag> {
    @TableId
    //标签ID
    private Long id;
    //标签名称
    private String name;
    //状态（0-正常，1-禁用）
    private String status;
    //删除标志（0-未删除，1-已删除）
    private String delFlag;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;

}

