package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.List;

/**
 * 添加通知请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "添加通知请求对象")
public class AddNoticeDto
{
    @NotBlank(message = "通知标题不能为空")
    @ApiModelProperty(value = "通知标题", required = true, example = "测试通知标题")
    private String title;

    @NotBlank(message = "通知内容不能为空")
    @ApiModelProperty(value = "通知内容（HTML格式/md格式）", required = true, example = "你好")
    private String content;

    @NotNull(message = "通知类型不能为空")
    @ApiModelProperty(value = "通知类型（关联字典编码：notice_type）", required = true, example = "1")
    private Integer type;

    @NotNull(message = "通知等级不能为空")
    @ApiModelProperty(value = "通知等级（LOW/MEDIUM/HIGH）（字典code：notice_level）", example = "H")
    private String level;

    @Pattern(regexp = "^$|^[012]$", message = "状态只能为0，1或2")
    @ApiModelProperty(value = "状态（0: 未发布, 1: 已发布, 2: 已撤回）", example = "1")
    private String status;

    @ApiModelProperty(value = "目标人ID集合（多个使用英文逗号,分割）", example = "['1','2']")
    private List<Long> targetUserIds;

    @NotNull(message = "目标类型")
    @Pattern(regexp = "^$|^[12]$", message = "目标类型只能为1或2")
    @ApiModelProperty(value = "目标类型（1: 全体, 2: 指定）", example = "1")
    private String targetType;
}
