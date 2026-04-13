package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddPostsDto;
import com.mengze.domain.dto.PostsListDto;
import com.mengze.domain.vo.PostsFormDetailVo;

/**
 * 后台文章管理服务接口
 *
 * @author mengze
 */
public interface AdminPostsService {

    ResponseResult postsList(Integer pageNum, Integer pageSize, PostsListDto postsListDto);

    ResponseResult<PostsFormDetailVo> postsDetail(Long id);

    ResponseResult addPosts(AddPostsDto postsDto);
    
    ResponseResult updatePosts(Long id, AddPostsDto postsDto);
}
