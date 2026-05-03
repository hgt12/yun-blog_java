package com.mengze.domain.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ReplyCommentDto {

    @NotNull(message = "父评论ID不能为空")
    @ApiModelProperty(value = "父评论id", required = true, example = "1")
    private Long parentId;

    @NotBlank(message = "评论内容不能为空")
    @ApiModelProperty(value = "评论内容", required = true, example = "这是评论内容")
    private String content;

    @ApiModelProperty(value = "跟评论id", example = "1")
    private Long rootId;

    @ApiModelProperty(value = "回复目标用户ID", example = "2")
    private Long replyToUserId;

    @ApiModelProperty(value = "回复目标评论的ID", example = "3")
    private Long replyToCommentId;

    @ApiModelProperty(value = "评论类型", example = "0")
    private String type;

    @ApiModelProperty(value = "文章id", example = "2")
    private Long articleId;
}
