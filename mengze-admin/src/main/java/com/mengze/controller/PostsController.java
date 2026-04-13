package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddPostsDto;
import com.mengze.domain.dto.PostsListDto;
import com.mengze.domain.vo.ArticleListVo;
import com.mengze.domain.vo.PostsFormDetailVo;
import com.mengze.service.AdminPostsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 后台文章管理接口
 */
@RestController
@RequestMapping("/posts")
@Api(tags = "后台文章管理", description = "后台文章管理相关接口")
public class PostsController
{
    @Autowired
    private AdminPostsService adminPostsService;

    @GetMapping
    @SystemLog(businessName = "后台分页查询文章列表")
    @ApiOperation(value = "后台文章列表", notes = "分页查询文章列表，支持多条件筛选", response = ArticleListVo.class, responseContainer = "List")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "categoryId", value = "分类id", dataType = "long", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "文章状态", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "keywords", value = "搜索关键字", dataType = "string", paramType = "query")
    })
    public ResponseResult<ArticleListVo> postsList(Integer pageNum, Integer pageSize, PostsListDto postsListDto)
    {
        return adminPostsService.postsList(pageNum, pageSize, postsListDto);
    }

    @GetMapping("/{id}/form")
    @SystemLog(businessName = "获取文章表单数据")
    @ApiOperation(value = "获取文章表单数据", notes = "获取文章表单详情数据")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "文章id", dataType = "long", paramType = "path")
    })
    public ResponseResult<PostsFormDetailVo> postsFormDetail(@PathVariable Long id)
    {
        return adminPostsService.postsDetail(id);
    }

    @PostMapping
    @SystemLog(businessName = "新增文章")
    @ApiOperation(value = "新增文章接口", notes = "新增文章")
    public ResponseResult addPosts(@RequestBody AddPostsDto postsDto)
    {
        return adminPostsService.addPosts(postsDto);
    }

    @PutMapping("/{id}")
    @SystemLog(businessName = "编辑/更新文章")
    @ApiOperation(value = "编辑/更新文章接口", notes = "更新文章，会自动删除旧的图片文件")
    @ApiImplicitParam(name = "id", value = "文章ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updatePosts(@PathVariable Long id, @RequestBody AddPostsDto postsDto)
    {
        return adminPostsService.updatePosts(id, postsDto);
    }
}
