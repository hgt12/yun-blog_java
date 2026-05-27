package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

/**
 * 通知列表请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "通知列表请求对象")
public class NoticeListDto
{
    @Pattern(regexp = "^$|^[012]$", message = "状态只能为0、1或2")
    @ApiModelProperty(value = "发布状态（0-草稿，1-已发布，2-已撤回）", example = "0")
    private String status;

    @Pattern(regexp = "^$|^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "是否已读（0-未读，1-已读）", example = "0")
    private String isRead;

    @ApiModelProperty(value = "通知标题关键字", example = "通知")
    private String title;
}
