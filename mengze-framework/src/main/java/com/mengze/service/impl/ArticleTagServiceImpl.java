package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.entity.ArticleTag;
import com.mengze.mapper.ArticleTagMapper;
import com.mengze.service.ArticleTagService;
import org.springframework.stereotype.Service;

/**
 * 文章标签关联表(ArticleTag)表服务实现类
 *
 * @author makejava
 * @since 2026-04-12 14:13:37
 */
@Service
public class ArticleTagServiceImpl extends ServiceImpl<ArticleTagMapper, ArticleTag> implements ArticleTagService {

}
