package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 访问日志表(VisitLog)表实体类
 *
 * @author makejava
 * @since 2026-03-15 19:13:19
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("visit_log")
public class VisitLog extends Model<VisitLog> {
    @TableId
    //日志ID
    private Long id;
    //访问者IP地址（脱敏存储）
    private String ip;
    //访问的页面URL
    private String pageUrl;
    //页面标题
    private String pageTitle;
    //来源页面
    private String referrer;
    //设备类型（PC/Mobile/Tablet）
    private String deviceType;
    //浏览器类型
    private String browser;
    //操作系统
    private String os;
    //访问时间
    private Date visitTime;

}

