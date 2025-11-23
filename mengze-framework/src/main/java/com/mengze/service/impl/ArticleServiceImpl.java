package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.mapper.ArticleMapper;
import com.mengze.domain.entity.Article;
import com.mengze.service.ArticleService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章表(Article)表服务实现类
 *
 * @author makejava
 * @since 2025-11-22 15:43:53
 */
@Service("articleService")
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    //热门文章列表方法，用mybatis_plus中的相关的一些查询，
    // 最后再把结果封装到ResponseResult对象当中统一响应格式
    @Override
    public ResponseResult hotArticleList() {
        //查询热门文章，封装成ResponseResult返回

        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件：必须是正式文章，不能是草稿
        queryWrapper.eq(Article::getStatus, 0);//0表示已发布状态
        //要进行降序排列
        queryWrapper.orderByDesc(Article::getViewCount);//通过获取观看次数进行排列
        //最多只能查询十条记录
        Page<Article> page = new Page<>(1, 10);
        page(page, queryWrapper);

        List<Article> list = page.getRecords();
        return ResponseResult.okResult(list);
    }
}

