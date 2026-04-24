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
 * 权限说明：
 * - 文章评论（type=0）：需要登录
 * - 友链评论（type=1）：需要登录
 * - 留言板评论（type=2）：不需要登录，但需要填写昵称和邮箱
 */
@RestController
@RequestMapping("/comment")
@Api(tags = "评论", description = "评论相关接口")
public class CommentController
{

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

    /**
     * 添加评论（支持文章评论、友链评论、留言板评论）
     * 请求体示例：
     * 文章评论（需登录）：{"type":"0", "articleId":1, "content":"评论内容", "rootId":-1}
     * 友链评论（需登录）：{"type":"1", "content":"评论内容", "rootId":-1}
     * 留言板评论（无需登录）：{"type":"2", "nickname":"昵称", "email":"xxx@email.com", "content":"评论内容", "rootId":-1}
     * 回复评论：{"type":"0", "articleId":1, "content":"回复内容", "rootId":1, "replyToCommentId":2}
     * 
     * @param addCommentDto 评论信息
     * @return 评论结果
     */
    @PostMapping
    @SystemLog(businessName = "添加评论")
    @ApiOperation(value = "添加评论", notes = "添加文章评论、友链评论或留言板评论")
    public ResponseResult addComment(@Valid @RequestBody AddCommentDto addCommentDto)
    {
        return commentService.addComment(addCommentDto);
    }

    /**
     * 点赞评论
     * @param commentId 评论ID
     * @return 更新后的点赞数
     */
    @PutMapping("/like/{commentId}")
    @SystemLog(businessName = "点赞评论")
    @ApiOperation(value = "点赞评论", notes = "对指定评论进行点赞")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "commentId", value = "评论id")
    })
    public ResponseResult likeComment(@PathVariable("commentId") Long commentId)
    {
        return commentService.likeComment(commentId);
    }

    /**
     * 生成评论头像（用于评论框预览）
     * 规则：
     * - 空邮箱：返回 d=mp（神秘人物轮廓）
     * - 邮箱格式错误：返回 d=identicon（彩色几何图案）
     * - 邮箱格式正确：返回对应邮箱的头像，默认使用 d=identicon
     * 
     * @param params 包含email参数的Map
     * @return 头像URL
     */
    @PostMapping("/generateAvatar")
    @SystemLog(businessName = "生成评论头像")
    @ApiOperation(value = "生成评论头像", notes = "根据邮箱生成评论框预览头像")
    public ResponseResult generateAvatar(@RequestBody Map<String, String> params)
    {
        String email = params.get("email");
        return commentService.generateAvatar(email);
    }
}
