package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 独立访客记录表(UniqueVisitor)表实体类
 *
 * @author makejava
 * @since 2026-03-15 19:12:12
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("unique_visitor")
public class UniqueVisitor extends Model<UniqueVisitor> {
    @TableId
    //主键ID
    private Long id;
    //访客标识哈希值（IP+User-Agent的MD5）
    private String visitorHash;
    //访问者IP地址
    private String ip;
    //首次访问时间
    private Date firstVisitTime;
    //最后访问时间
    private Date lastVisitTime;
    //访问次数
    private Integer visitCount;

}

