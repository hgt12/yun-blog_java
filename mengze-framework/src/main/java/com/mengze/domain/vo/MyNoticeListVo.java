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

/**
 * 我的通知Vo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@ApiModel(description = "我的通知列表响应对象")
public class MyNoticeListVo
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

    @ApiModelProperty(value = "状态（0: 未发布, 1: 已发布, 2: 已撤回）", example = "0")
    private String status;

    @TableField(exist = false)
    @ApiModelProperty(value = "读取状态（0: 未读, 1: 已读）", example = "0")
    private String isRead;

    @ApiModelProperty(value = "发布人名字", example = "云梦泽")
    private String publisherName;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    @ApiModelProperty(value = "发布时间", example = "2024-01-01 12:00:00")
    private Date publishTime;

}
