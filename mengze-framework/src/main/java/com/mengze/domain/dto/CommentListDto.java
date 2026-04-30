package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

/**
 * 评论列表请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "评论列表请求对象")
public class CommentListDto {

    @ApiModelProperty(value = "关键字（用于模糊搜索评论内容）", example = "测试")
    private String keywords;

    @Pattern(regexp = "^$|^[012]$", message = "评论类型只能为0、1或2")
    @ApiModelProperty(value = "评论类型（0-文章评论，1-友链留言，2-留言板）", example = "0")
    private String type;

    @Pattern(regexp = "^$|^[01]$", message = "状态只能为0或1")
    @ApiModelProperty(value = "审核状态（0-正常，1-待审核）", example = "0")
    private String status;

    @ApiModelProperty(value = "文章ID", example = "123")
    private String articleId;

    @ApiModelProperty(value = "开始时间", example = "2026-04-01 00:00:00")
    private String startTime;

    @ApiModelProperty(value = "结束时间", example = "2026-04-30 23:59:59")
    private String endTime;
}
