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

/**
 * 用户导入模板导出VO
 *
 * 用于下载Excel导入模板，提供字段表头和示例数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserImportTemplateVo
{
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

    @ColumnWidth(20)
    @ExcelProperty("性别(男-0,女-1,隐藏-2)")
    @ApiModelProperty(value = "用户性别，（0 男，1 女，2 隐藏）", example = "0")
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    private String sex;

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

}
