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
 * 访问日志表(VisitLog)表实体类
 *
 * @author makejava
 * @since 2026-05-16 22:32:17
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
    //访问者IP地址（不明文入库）
    private String ip;
    //操作人（游客/用户（显示用户名））
    private String operator;
    //页面标题
    private String pageTitle;
    //访问的页面URL（页面地址）
    private String pageUrl;
    //来源页面（页面前地址）
    private String referrer;
    //请求方法
    private String method;
    //设备类型（PC/Mobile/Tablet）
    private String deviceType;
    //地区（国内外）
    private String region;
    //浏览器类型
    private String browser;
    //操作系统
    private String os;
    //访问时间
    private Date visitTime;
    //页面执行时间（毫秒）
    private Integer executionTime;

}

