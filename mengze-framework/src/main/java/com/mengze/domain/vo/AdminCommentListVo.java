package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 后台评论管理VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "后台评论管理响应对象")
public class AdminCommentListVo {
    
    @ApiModelProperty(value = "评论ID", example = "1")
    private String id;
    
    @ApiModelProperty(value = "评论类型（0-文章评论，1-友链留言，2-留言板）", example = "0")
    private String type;
    
    @ApiModelProperty(value = "文章ID", example = "1")
    private String articleId;
    
    @ApiModelProperty(value = "文章标题", example = "Spring Boot入门教程")
    private String articleTitle;
    
    @ApiModelProperty(value = "用户ID", example = "1")
    private String userId;
    
    @ApiModelProperty(value = "昵称", example = "张三")
    private String nickname;
    
    @ApiModelProperty(value = "邮箱", example = "user@example.com")
    private String email;
    
    @ApiModelProperty(value = "个人网址", example = "https://example.com")
    private String personalWebsite;
    
    @ApiModelProperty(value = "评论内容", example = "这篇文章写得很好！")
    private String content;
    
    @ApiModelProperty(value = "根评论ID", example = "1")
    private Long rootId;
    
    @ApiModelProperty(value = "回复目标评论的用户昵称", example = "李四")
    private String replyToNickname;
    
    @ApiModelProperty(value = "点赞数", example = "10")
    private Integer likeCount;
    
    @ApiModelProperty(value = "审核状态（0-正常，1-待审核，2-审核未通过）", example = "0")
    private String status;
    
    @ApiModelProperty(value = "创建时间", example = "2024-01-01 12:00:00")
    private String createTime;
    
    @ApiModelProperty(value = "子评论列表")
    private List<AdminCommentListVo> children;
}
