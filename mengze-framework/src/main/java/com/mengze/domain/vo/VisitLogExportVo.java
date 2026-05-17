package com.mengze.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 导出日志VO
 * 用于EasyExcel导出日志数据
 *
 * @author mengze
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "日志导出响应对象")
@ExcelIgnoreUnannotated  // 只导出有@ExcelProperty注解的字段
public class VisitLogExportVo
{
    @ColumnWidth(10)
    @ExcelProperty(value = "日志ID", index = 0)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "日志ID", example = "1")
    private Long id;

    @ColumnWidth(15)
    @ExcelProperty(value = "操作人", index = 1)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "操作人（游客/用户（显示用户名））", example = "云梦泽")
    private String operator;

    @ColumnWidth(25)
    @ExcelProperty(value = "访问时间", index = 2)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "访问时间", example = "2026-05-16 19:20:30")
    private Date visitTime;

    @ColumnWidth(15)
    @ExcelProperty(value = "页面标题", index = 3)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "页面标题", example = "文章")
    private String pageTitle;

    @ColumnWidth(15)
    @ExcelProperty(value = "页面地址", index = 4)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "访问的页面URL（页面地址）", example = "/post")
    private String pageUrl;

    @ColumnWidth(15)
    @ExcelProperty(value = "页面前地址", index = 5)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "来源页面（页面前地址）", example = "/log")
    private String referrer;

    @ColumnWidth(15)
    @ExcelProperty(value = "请求方法", index = 6)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "请求方法", example = "POST")
    private String method;

    @ColumnWidth(30)
    @ExcelProperty(value = "IP地址", index = 7)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "访问者IP地址（不明文入库）", example = "127.0.0.0")
    private String ip;

    @ColumnWidth(30)
    @ExcelProperty(value = "地区信息", index = 8)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "地区（国内外）", example = "中国|xx省|xx市|")
    private String region;

    @ColumnWidth(15)
    @ExcelProperty(value = "浏览器类型", index = 9)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "浏览器类型", example = "Edge")
    private String browser;

    @ColumnWidth(15)
    @ExcelProperty(value = "操作系统", index = 10)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "操作系统", example = "Windows")
    private String os;

    @ColumnWidth(15)
    @ExcelProperty(value = "设备类型", index = 11)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "设备类型", example = "PC")
    private String deviceType;


    @ColumnWidth(20)
    @ExcelProperty(value = "页面执行时间", index = 12)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ApiModelProperty(value = "页面执行时间", example = "5")
    private Integer executionTime;

}
