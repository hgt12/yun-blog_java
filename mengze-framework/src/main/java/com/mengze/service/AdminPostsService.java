package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.PostsListDto;

/**
 * 后台文章管理服务接口
 *
 * @author mengze
 */
public interface AdminPostsService {

    /**
     * 后台分页查询文章列表（支持多条件筛选）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param postsListDto 查询条件DTO
     * @return 文章列表
     */
    ResponseResult postsList(Integer pageNum, Integer pageSize, PostsListDto postsListDto);
}
