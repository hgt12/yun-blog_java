package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.CommentVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.mapper.CommentMapper;
import com.mengze.domain.entity.Comment;
import com.mengze.service.CommentService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评论表(Comment)表服务实现类
 *
 * @author makejava
 * @since 2026-01-12 15:48:35
 */
@Service("commentService")
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService
{

    //获取评论列表（统一接口，文章评论、友链评论、留言板评论）
    @Override
    public ResponseResult commentList(Integer type, Long articleId, Integer pageNum, Integer pageSize)
    {
        //查询根评论（rootId为-1的评论）
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getType, String.valueOf(type));
        //文章评论需要关联articleId
        if (SystemConstants.COMMENT_TYPE_ARTICLE.equals(String.valueOf(type))) {
            queryWrapper.eq(Comment::getArticleId, articleId);
        }
        queryWrapper.eq(Comment::getRootId, SystemConstants.COMMENT_ROOT_ID);
        queryWrapper.eq(Comment::getStatus, SystemConstants.COMMENT_STATUS_NORMAL);
        queryWrapper.orderByDesc(Comment::getCreateTime);

        //分页查询
        Page<Comment> page = new Page<>(pageNum, pageSize);
        page(page, queryWrapper);

        //转换为VO
        List<CommentVo> commentVoList = toCommentVoList(page.getRecords());

        //查询子评论
        for (CommentVo commentVo : commentVoList) {
            List<CommentVo> children = getChildren(commentVo.getId());
            commentVo.setChildren(children);
        }

        return ResponseResult.okResult(new PageVo(commentVoList, page.getTotal()));
    }

    //获取子评论列表
    private List<CommentVo> getChildren(Long rootId)
    {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getRootId, rootId);
        queryWrapper.eq(Comment::getStatus, SystemConstants.COMMENT_STATUS_NORMAL);
        queryWrapper.orderByAsc(Comment::getCreateTime);
        List<Comment> comments = list(queryWrapper);
        return toCommentVoList(comments);
    }

    //转换为CommentVo列表
    private List<CommentVo> toCommentVoList(List<Comment> list)
    {
        List<CommentVo> commentVos = BeanCopyUtils.copyBeanList(list, CommentVo.class);
        // 设置回复目标评论的昵称
        for (CommentVo commentVo : commentVos)
        {
            if (commentVo.getReplyToCommentId() != null && commentVo.getReplyToCommentId() > 0)
            {
                Comment replyToComment = getById(commentVo.getReplyToCommentId());
                if (replyToComment != null) {
                    commentVo.setReplyToCommentNickname(replyToComment.getNickname());
                }
            }
        }
        return commentVos;
    }
}
