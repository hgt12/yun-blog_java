package com.mengze.domain.vo;

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
public class CommentVo {
    //评论ID
    private Long id;
    //评论类型
    private String type;
    //文章ID
    private Long articleId;
    //用户ID
    private Long userId;
    //用户类型（0表示普通用户，1表示管理员/博主）
    private String userType;
    //昵称
    private String nickname;
    //头像URL
    private String avatar;
    //评论内容
    private String content;
    //根评论ID
    private Long rootId;
    //回复目标评论ID
    private Long replyToCommentId;
    //回复目标评论的用户昵称
    private String replyToCommentNickname;
    //回复目标评论的用户类型（0表示普通用户，1表示博主/管理员）
    private String replyToCommentUserType;
    //点赞数
    private Integer likeCount;
    //创建时间
    private Date createTime;
    //子评论列表
    private List<CommentVo> children;
}
