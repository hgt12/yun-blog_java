package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网站统计表(SiteStatistics)表实体类
 *
 * @author makejava
 * @since 2026-03-15 19:04:21
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("site_statistics")
public class SiteStatistics extends Model<SiteStatistics> {
    @TableId
    //主键ID
    private Long id;
    //总访问量（PV）
    private Long totalViews;
    //总访客数（UV）
    private Long totalVisitors;
    //更新时间
    private Date updateTime;

}

