package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddPostsDto;
import com.mengze.domain.dto.PostsListDto;
import com.mengze.domain.dto.UpdatePostsDto;
import com.mengze.domain.vo.ArticleListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.PostsFormDetailVo;
import com.mengze.service.AdminPostsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    @ApiOperation(value = "后台文章列表", notes = "分页查询文章列表，支持多条件筛选", response = PageVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "pageNum", value = "页号", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "pageSize", value = "每页数量", dataType = "int", paramType = "query")
    })
    public ResponseResult<ArticleListVo> getPostsList(Integer pageNum, Integer pageSize, @Valid @RequestBody PostsListDto postsListDto)
    {
        return adminPostsService.getPostsList(pageNum, pageSize, postsListDto);
    }

    @GetMapping("/{id}/form")
    @SystemLog(businessName = "获取文章表单数据")
    @ApiOperation(value = "获取文章表单数据", notes = "获取文章表单详情数据", response = PostsFormDetailVo.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "文章id", dataType = "long", paramType = "path")
    })
    public ResponseResult<PostsFormDetailVo> getPostsFormDetail(@PathVariable Long id)
    {
        return adminPostsService.getPostsDetail(id);
    }

    @PostMapping
    @SystemLog(businessName = "新增文章")
    @ApiOperation(value = "新增文章接口", notes = "新增文章", response = String.class)
    public ResponseResult addPosts(@Valid @RequestBody AddPostsDto postsDto)
    {
        return adminPostsService.addPosts(postsDto);
    }

    @PutMapping("/{id}")
    @SystemLog(businessName = "编辑/更新文章")
    @ApiOperation(value = "编辑/更新文章接口", notes = "更新文章，会自动删除旧的图片文件", response = String.class)
    @ApiImplicitParam(name = "id", value = "文章ID", dataType = "long", paramType = "path", required = true)
    public ResponseResult updatePosts(@PathVariable Long id, @Valid @RequestBody UpdatePostsDto updatePostsDto)
    {
        return adminPostsService.updatePosts(id, updatePostsDto);
    }

    @DeleteMapping("/{ids}")
    @SystemLog(businessName = "删除文章(逻辑删除)")
    @ApiOperation(value = "删除文章接口（批量逻辑删除）", notes = "删除文章，支持批量删除，会自动删除相关的图片文件和标签关联", response = String.class)
    @ApiImplicitParam(name = "ids", value = "文章id", dataType = "Long", paramType = "path", required = true)
    public ResponseResult deletePosts(@PathVariable Long[] ids)
    {
        return adminPostsService.deletePosts(ids);
    }
}
