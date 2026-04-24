package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 添加评论请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "添加评论请求对象")
public class AddCommentDto {
    
    @NotBlank(message = "评论类型不能为空")
    @Pattern(regexp = "^[012]$", message = "评论类型只能为0、1或2")
    @ApiModelProperty(value = "评论类型（0-文章评论，1-友链评论，2-留言板评论）", required = true, example = "0")
    private String type;
    
    @ApiModelProperty(value = "文章ID（type=0时必填）", example = "1")
    private Long articleId;
    
    @ApiModelProperty(value = "昵称（留言板评论时必填）", example = "访客")
    private String nickname;
    
    @Pattern(regexp = "^$|^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    @ApiModelProperty(value = "邮箱（留言板评论时必填）", example = "visitor@example.com")
    private String email;
    
    @Pattern(regexp = "^$|^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", message = "个人网址格式不正确")
    @ApiModelProperty(value = "个人网址", example = "https://example.com")
    private String personalWebsite;
    
    @NotBlank(message = "评论内容不能为空")
    @ApiModelProperty(value = "评论内容", required = true, example = "这是一条评论")
    private String content;
    
    @NotNull(message = "根评论ID不能为空")
    @ApiModelProperty(value = "根评论ID（-1表示顶级评论）", required = true, example = "-1")
    private Long rootId;
    
    @ApiModelProperty(value = "父评论ID", example = "1")
    private Long parentId;
    
    @ApiModelProperty(value = "回复目标评论的ID", example = "2")
    private Long replyToCommentId;
    
    @ApiModelProperty(value = "回复目标用户ID", example = "3")
    private Long replyToUserId;
}
