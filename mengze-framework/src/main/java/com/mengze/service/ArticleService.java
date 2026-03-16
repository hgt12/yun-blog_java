package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Article;

/**
 * 文章表(Article)表服务接口
 *
 * @author makejava
 * @since 2025-11-22 15:33:19
 */

public interface ArticleService extends IService<Article> {

    ResponseResult hotArticleList();

    ResponseResult articleList(Integer pageNum, Integer pageSize, Long categoryId);

    ResponseResult getArticleDetail(Long id);

    ResponseResult articleListByCategoryId(Long categoryId, Integer pageNum, Integer pageSize);

    ResponseResult viewCount(Long id);

    //esponseResult articleListByTagId(Long tagId, Integer pageNum, Integer pageSize);
}

