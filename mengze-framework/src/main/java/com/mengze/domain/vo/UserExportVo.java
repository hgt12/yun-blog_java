package com.mengze.domain.vo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.alibaba.fastjson.annotation.JSONField;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户数据导出VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserExportVo
{
    @ColumnWidth(10)
    @ExcelProperty("用户id")
    @ApiModelProperty(value = "用户id", example = "1")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private Long id;

    @ColumnWidth(15)
    @ExcelProperty("用户名")
    @ApiModelProperty(value = "用户名", example = "云梦泽")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private String username;

    @ColumnWidth(15)
    @ExcelProperty("昵称")
    @ApiModelProperty(value = "昵称", example = "云梦泽")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private String nickname;

    @ColumnWidth(10)
    @ExcelProperty("性别")
    @ApiModelProperty(value = "用户性别，（0 男，1 女，2 隐藏）", example = "0")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private String sex;

    @ColumnWidth(15)
    @ExcelProperty("部门名称")
    @ApiModelProperty(value = "部门名称", example = "测试不")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private String deptName;

    @ColumnWidth(15)
    @ExcelProperty("角色名称")
    @ApiModelProperty(value = "角色名称，多个使用英文逗号(,)分割", example = "系统管理员")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private String roleNames;

    @ColumnWidth(30)
    @ExcelProperty("邮箱")
    @ApiModelProperty(value = "用户邮箱", example = "admin@example.com")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private String email;

    @ColumnWidth(25)
    @ExcelProperty("手机号")
    @ApiModelProperty(value = "手机号", example = "13800138000")
    @JSONField(name = "mobile")//转换成 mobile 再响应给前端
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private String phone;

    @ColumnWidth(15)
    @ExcelProperty("账号状态")
    @ApiModelProperty(value = "账号状态（0 正常，1 停用）", example = "0")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private String status;

    @ColumnWidth(20)
    @ExcelProperty("创建时间")
    @ApiModelProperty(value = "创建时间", example = "2026-05-20 21:58:53")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private Date createTime;

}
