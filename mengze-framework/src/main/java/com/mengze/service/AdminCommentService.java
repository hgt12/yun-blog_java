package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.CommentListDto;
import com.mengze.domain.dto.ReplyCommentDto;

/**
 * 后台评论管理服务接口
 *
 * @author mengze
 */
public interface AdminCommentService {
    ResponseResult getCommentList(Integer pageNum, Integer pageSize, CommentListDto commentListDto);

    ResponseResult replyComment(ReplyCommentDto replyCommentDto);

    ResponseResult deleteComment(Long[] ids);

    ResponseResult approveComment(Long[] ids);
}
