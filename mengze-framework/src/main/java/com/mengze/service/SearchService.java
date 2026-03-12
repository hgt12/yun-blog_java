package com.mengze.service;

import com.mengze.domain.ResponseResult;

/**
 * 友链(Link)表服务接口
 *
 * @author makejava
 * @since 2026-3-8 16:38:30
 */
public interface SearchService
{
    ResponseResult search(Integer pageNum, Integer pageSize, String keyword);
}
