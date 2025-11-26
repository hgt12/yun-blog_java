package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
}
