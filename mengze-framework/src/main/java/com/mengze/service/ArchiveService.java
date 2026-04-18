package com.mengze.service;

import com.mengze.domain.ResponseResult;

public interface ArchiveService
{
    /**
     * 获取归档列表（分页）
     * @param pageNum 页号
     * @param pageSize 每页数量
     * @return 文章列表（按创建时间倒序）
     */
    ResponseResult getArchiveList(Integer pageNum, Integer pageSize);
}
