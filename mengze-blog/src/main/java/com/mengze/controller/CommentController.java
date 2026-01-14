package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Comment;
import com.mengze.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评论接口
 * 
 * 权限说明：
 * - 文章评论（type=0）：需要登录
 * - 友链评论（type=1）：需要登录
 * - 留言板评论（type=2）：不需要登录，但需要填写昵称和邮箱
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取评论列表（统一接口，支持文章评论、友链评论、留言板评论）
     * @param type 评论类型（0-文章评论，1-友链评论，2-留言板评论）
     * @param articleId 文章ID（type=0时必填）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评论列表
     */
    @GetMapping("/commentList")
    public ResponseResult commentList(@RequestParam Integer type,
                                      @RequestParam(required = false) Long articleId,
                                      @RequestParam Integer pageNum,
                                      @RequestParam(defaultValue = "20") Integer pageSize) {
        return commentService.commentList(type, articleId, pageNum, pageSize);
    }

    /**
     * 添加评论（支持文章评论、友链评论、留言板评论）
     * 请求体示例：
     * 文章评论（需登录）：{"type":"0", "articleId":1, "content":"评论内容", "rootId":-1}
     * 友链评论（需登录）：{"type":"1", "content":"评论内容", "rootId":-1}
     * 留言板评论（无需登录）：{"type":"2", "nickname":"昵称", "email":"xxx@email.com", "content":"评论内容", "rootId":-1}
     * 回复评论：{"type":"0", "articleId":1, "content":"回复内容", "rootId":1, "replyToCommentId":2}
     * 
     * @param comment 评论信息
     * @return 评论结果
     */
    @PostMapping
    public ResponseResult addComment(@RequestBody Comment comment)
    {
        return commentService.addComment(comment);
    }

    /**
     * 点赞评论
     * @param commentId 评论ID
     * @return 更新后的点赞数
     */
    @PutMapping("/like/{commentId}")
    public ResponseResult likeComment(@PathVariable("commentId") Long commentId)
    {
        return commentService.likeComment(commentId);
    }
}
