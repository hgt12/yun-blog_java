package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Article;
import com.mengze.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//与文章相关的接口
@RestController
@RequestMapping("/article")//对应本地链接http://localhost:1314/article/
public class ArticleController {

    @Autowired
    private ArticleService articleService;

//    @GetMapping("/list")
//    public List<Article> list() {
//        return articleService.list();
//    }

    //先在Controller中定义对应的接口方法，具体业务逻辑的代码在Service方法里实现
    //然后再去写Service方法
    //热门文档查询接口
    @GetMapping("/hotArticleList")
    public ResponseResult hotArticleList()
    {
        ResponseResult result = articleService.hotArticleList();
        return result;
    }

    //分页查询文章列表（要有分类id，页码，每页大小参数）
    @GetMapping("/articleList")
    public  ResponseResult articleList(Integer pageNum, Integer pageSize, Long categoryId)
    {
        return articleService.articleList(pageNum, pageSize, categoryId);
    }

    //文章详情接口
    @GetMapping("/{id}")
    public  ResponseResult getArticleDetail(@PathVariable("id") Long id)
    {
        return articleService.getArticleDetail(id);
    }
}
