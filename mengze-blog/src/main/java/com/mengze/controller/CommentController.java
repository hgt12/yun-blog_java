package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 评论接口
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

}
