package com.mengze.service;

import com.mengze.domain.ResponseResult;

public interface ArchiveService
{
    /**
     * 获取归档列表（按年月分组）
     */
    ResponseResult getArchiveList();

    /**
     * 根据年月获取文章列表
     */
    ResponseResult getArticlesByYearMonth(Integer year, Integer month, Integer pageNum, Integer pageSize);
}
