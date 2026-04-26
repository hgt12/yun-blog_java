package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 评论VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "评论响应对象")
public class CommentVo {
    
    @ApiModelProperty(value = "评论ID")
    private Long id;
    
    @ApiModelProperty(value = "评论类型（0-文章评论，1-友链评论，2-留言板评论）")
    private String type;
    
    @ApiModelProperty(value = "文章ID")
    private Long articleId;
    
    @ApiModelProperty(value = "用户ID")
    private Long userId;
    
    @ApiModelProperty(value = "用户角色列表")
    private List<String> roles;
    
    @ApiModelProperty(value = "昵称")
    private String nickname;
    
    @ApiModelProperty(value = "头像URL")
    private String avatar;
    
    @ApiModelProperty(value = "评论内容")
    private String content;
    
    @ApiModelProperty(value = "根评论ID")
    private Long rootId;
    
    @ApiModelProperty(value = "回复目标评论ID")
    private Long replyToCommentId;
    
    @ApiModelProperty(value = "回复目标评论的用户昵称")
    private String replyToCommentNickname;
    
    @ApiModelProperty(value = "回复目标评论的用户角色列表")
    private List<String> replyToCommentRoles;
    
    @ApiModelProperty(value = "点赞数")
    private Integer likeCount;
    
    @ApiModelProperty(value = "创建时间")
    private Date createTime;
    
    @ApiModelProperty(value = "子评论列表")
    private List<CommentVo> children;
}
