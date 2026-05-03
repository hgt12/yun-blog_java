package com.mengze.controller;

import ch.qos.logback.core.spi.FilterReply;
import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.CommentListDto;
import com.mengze.domain.dto.ReplyCommentDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.AdminCommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * 评论管理接口
 */
@RestController
@RequestMapping("/comments")
@Api(tags = "评论管理", description = "评论管理相关接口")
public class CommentController
{
    @Autowired
    private AdminCommentService adminCommentService;

    @GetMapping
    @SystemLog(businessName = "分页查询评论列表")
    @ApiOperation(value = "评论列表", notes = "分页查询评论列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "keywords", value = "搜索关键字", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "type", value = "评论类型", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "审核状态", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "articleId", value = "文章ID", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "startTime", value = "开始时间", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "endTime", value = "结束时间", dataType = "string", paramType = "query")
    })
    public ResponseResult getCommentList(Integer pageNum, Integer pageSize, @Valid CommentListDto commentListDto)
    {
        return adminCommentService.getCommentList(pageNum, pageSize, commentListDto);
    }

    @PostMapping
    @SystemLog(businessName = "回复评论")
    @ApiOperation(value = "回复评论", notes = "管理员回复评论")
    public ResponseResult replyComment(@RequestBody @Valid ReplyCommentDto replyCommentDto)
    {
        return adminCommentService.replyComment(replyCommentDto);
    }
}
