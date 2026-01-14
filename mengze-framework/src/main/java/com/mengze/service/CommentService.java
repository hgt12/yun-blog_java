package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Comment;

/**
 * 评论表(Comment)表服务接口
 */
public interface CommentService extends IService<Comment>
{

    /**
     * 获取评论列表（统一接口，支持文章评论、友链评论、留言板评论）
     * @param type 评论类型（0-文章评论，1-友链评论，2-留言板评论）
     * @param articleId 文章ID（type=0时必填）
     * @param pageNum 页码
     * @param pageSize 每页数量
     * @return 评论列表
     */
    ResponseResult commentList(Integer type, Long articleId, Integer pageNum, Integer pageSize);

    /**
     * 添加评论
     * @param comment 评论信息
     * @return 评论结果
     */
    ResponseResult addComment(Comment comment);

    ResponseResult likeComment(Long commentId);
}
