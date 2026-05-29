package com.mengze.domain.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "通知详情响应对象")
public class NoticeFormDetailVo
{
    @ApiModelProperty(value = "通知id", example = "6")
    private Long id;

    @ApiModelProperty(value = "通知标题", example = "这里是标题")
    private String title;

    @ApiModelProperty(value = "通知内容", example = "这里是内容")
    private String content;

    @ApiModelProperty(value = "通知类型（关联字典编码：notice_type）", example = "系统维护")
    private Integer type;

    @ApiModelProperty(value = "通知等级（字典code：notice_level）", example = "M")
    private String level;

    @ApiModelProperty(value = "状态（0: 未发布, 1: 已发布, 2: 已撤回）", example = "1")
    private String status;

    @ApiModelProperty(value = "目标人ID集合（多个使用英文逗号,分割）", example = "['1', '23']")
    private List<Long> targetUserIds;

    @ApiModelProperty(value = "目标类型（1: 全体, 2: 指定）", example = "1")
    private String targetType;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "发布时间", example = "2024-01-01 12:00:00")
    private Date publishTime;
}
