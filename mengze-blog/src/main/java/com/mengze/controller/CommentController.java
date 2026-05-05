package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddCommentDto;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.CommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * 评论接口
 */
@RestController
@RequestMapping("/comment")
@Api(tags = "评论", description = "评论相关接口")
public class CommentController
{

    @Autowired
    private CommentService commentService;

    @GetMapping("/commentList")
    @SystemLog(businessName = "获取评论列表")
    @ApiOperation(value = "评论列表", notes = "获取评论列表", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "type",value = "评论类型"),
            @ApiImplicitParam(name = "articleId",value = "文章id"),
            @ApiImplicitParam(name = "pageNum",value = "页号"),
            @ApiImplicitParam(name = "pageSize",value = "每页数量")
    })
    public ResponseResult commentList(@RequestParam Integer type,
                                      @RequestParam(required = false) Long articleId,
                                      @RequestParam Integer pageNum,
                                      @RequestParam(defaultValue = "20") Integer pageSize) {
        return commentService.commentList(type, articleId, pageNum, pageSize);
    }

    @PostMapping
    @SystemLog(businessName = "添加评论")
    @ApiOperation(value = "添加评论", notes = "添加文章评论、友链评论或留言板评论", response = String.class)
    public ResponseResult addComment(@Valid @RequestBody AddCommentDto addCommentDto)
    {
        return commentService.addComment(addCommentDto);
    }

    @PutMapping("/like/{commentId}")
    @SystemLog(businessName = "点赞评论")
    @ApiOperation(value = "点赞评论", notes = "对指定评论进行点赞", response = String.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "评论id")
    })
    public ResponseResult likeComment(@PathVariable("commentId") Long commentId)
    {
        return commentService.likeComment(commentId);
    }

    @PostMapping("/generateAvatar")
    @SystemLog(businessName = "生成评论头像")
    @ApiOperation(value = "生成评论头像", notes = "根据邮箱生成评论框预览头像", response = String.class)
    public ResponseResult generateAvatar(@RequestBody Map<String, String> params)
    {
        String email = params.get("email");
        return commentService.generateAvatar(email);
    }
}
