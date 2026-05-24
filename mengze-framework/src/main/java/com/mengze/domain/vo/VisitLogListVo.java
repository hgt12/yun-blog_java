package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "日志响应对象")
public class VisitLogListVo
{
    @ApiModelProperty(value = "日志ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "访问者IP地址", example = "127.0.0.0")
    private String ip;

    @ApiModelProperty(value = "操作人（游客/用户（显示用户名））", example = "云梦泽")
    private String operator;

    @ApiModelProperty(value = "访问的页面URL", example = "/post")
    private String pageUrl;

    @ApiModelProperty(value = "页面标题", example = "文章")
    private String pageTitle;

    @ApiModelProperty(value = "来源页面（页面前地址）", example = "/log")
    private String referrer;

    @ApiModelProperty(value = "设备类型（PC/Mobile/Tablet）", example = "PC")
    private String deviceType;

    @ApiModelProperty(value = "地区（国内外）", example = "中国|xx省|xx市|")
    private String region;

    @ApiModelProperty(value = "浏览器类型", example = "Edge")
    private String browser;

    @ApiModelProperty(value = "操作系统", example = "Windows")
    private String os;

    @ApiModelProperty(value = "访问时间", example = "2026-05-16 19:20:30")
    private Date visitTime;

    @ApiModelProperty(value = "页面执行时间（毫秒）", example = "5")
    private Integer executionTime;

    @ApiModelProperty(value = "请求方法", example = "POST")
    private String method;
}
