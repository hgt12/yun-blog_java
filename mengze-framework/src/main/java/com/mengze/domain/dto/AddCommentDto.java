package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 添加评论请求DTO
 */
@Data
@ApiModel(description = "添加评论请求对象")
public class AddCommentDto {
    
    @ApiModelProperty(value = "评论类型（0-文章评论，1-友链评论，2-留言板评论）", required = true, example = "0")
    private String type;
    
    @ApiModelProperty(value = "文章ID（type=0时必填）", example = "1")
    private Long articleId;
    
    @ApiModelProperty(value = "昵称（留言板评论时必填）", example = "访客")
    private String nickname;
    
    @ApiModelProperty(value = "邮箱（留言板评论时必填）", example = "visitor@example.com")
    private String email;
    
    @ApiModelProperty(value = "个人网址", example = "https://example.com")
    private String personalWebsite;
    
    @ApiModelProperty(value = "评论内容", required = true, example = "这是一条评论")
    private String content;
    
    @ApiModelProperty(value = "根评论ID（-1表示顶级评论）", required = true, example = "-1")
    private Long rootId;
    
    @ApiModelProperty(value = "父评论ID", example = "1")
    private Long parentId;
    
    @ApiModelProperty(value = "回复目标评论的ID", example = "2")
    private Long replyToCommentId;
    
    @ApiModelProperty(value = "回复目标用户ID", example = "3")
    private Long replyToUserId;
}
