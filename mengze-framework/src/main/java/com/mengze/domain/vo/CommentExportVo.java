package com.mengze.domain.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 导出评论VO
 * 用于EasyExcel导出评论数据
 *
 * @author mengze
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "评论导出响应对象")
@ExcelIgnoreUnannotated  // 只导出有@ExcelProperty注解的字段
public class CommentExportVo
{
    @ExcelProperty(value = "评论ID", index = 0)
    @ColumnWidth(15)
    private String id;
    
    @ExcelProperty(value = "评论类型", index = 1)
    @ColumnWidth(12)
    private String typeName;
    
    @ExcelProperty(value = "文章标题", index = 2)
    @ColumnWidth(30)
    private String articleTitle;
    
    @ExcelProperty(value = "昵称", index = 3)
    @ColumnWidth(15)
    private String nickname;
    
    @ExcelProperty(value = "邮箱", index = 4)
    @ColumnWidth(25)
    private String email;
    
    @ExcelProperty(value = "个人网址", index = 5)
    @ColumnWidth(30)
    private String personalWebsite;
    
    @ExcelProperty(value = "评论内容", index = 6)
    @ColumnWidth(50)
    private String content;
    
    @ExcelProperty(value = "回复目标", index = 7)
    @ColumnWidth(15)
    private String replyToNickname;
    
    @ExcelProperty(value = "点赞数", index = 8)
    @ColumnWidth(10)
    private Integer likeCount;
    
    @ExcelProperty(value = "审核状态", index = 9)
    @ColumnWidth(12)
    private String statusName;
    
    @ExcelProperty(value = "创建时间", index = 10)
    @ColumnWidth(20)
    private String createTime;
}
