package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.ArticleTag;
import com.mengze.domain.entity.Category;
import com.mengze.domain.entity.User;
import com.mengze.domain.vo.ArticleDetailVo;
import com.mengze.domain.vo.ArticleListVo;
import com.mengze.domain.vo.HotArticleListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.mapper.ArticleMapper;
import com.mengze.domain.entity.Article;
import com.mengze.mapper.ArticleTagMapper;
import com.mengze.service.ArticleService;
import com.mengze.service.CategoryService;
import com.mengze.service.UserService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章表(Article)表服务实现类
 *
 * @author makejava
 * @since 2025-11-22 15:43:53
 */
@Service("articleService")
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Autowired
    @Lazy
    private CategoryService categoryService;

    @Autowired
    @Lazy
    private UserService userService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    //热门文章列表方法，用mybatis_plus中的相关的一些查询，
    // 最后再把结果封装到ResponseResult对象当中统一响应格式
    @Override
    public ResponseResult hotArticleList() {
        //查询热门文章，封装成ResponseResult返回

        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        //查询条件：必须是正式文章，不能是草稿
        queryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);//0表示已发布状态(直接引用常量值)
        //要进行降序排列
        queryWrapper.orderByDesc(Article::getViewCount);//通过获取观看次数进行排列
        //最多只能查询十条记录
        Page<Article> page = new Page<>(1, 10);
        page(page, queryWrapper);

        List<Article> list = page.getRecords();

        //从redis中获取viewCount
        list.forEach(article -> {
            Integer viewCount = redisCache.getCacheMapValue(SystemConstants.ARTICLE_VIEW_COUNT, article.getId().toString());
            article.setViewCount(viewCount.longValue());
        });

        //使用bean拷贝封装Vo（需要注意的是，使用bean拷贝时要求拷贝前后的字段名和字段类型是要一致的，否则无法拷贝）
        //目前先在该类中实现，后续接口多了再把他封装成一个接口
//        List<HotArticleListVo> articleVos = new ArrayList<>();
//        for (Article article : list) {
//            HotArticleListVo hotArticleListVo = new HotArticleListVo();
//            BeanUtils.copyProperties(article, hotArticleListVo);
//            articleVos.add(hotArticleListVo);
//        }
//        BeanUtils.copyProperties(this, page.getRecords().get(0));

        //使用BeanCopyUtils工具类中的方法来对bean进行拷贝封装
        List<HotArticleListVo> hotArticleListVos = BeanCopyUtils.copyBeanList(list, HotArticleListVo.class);

        return ResponseResult.okResult(hotArticleListVos);
    }

    //分页查询文章列表
    @Override
    public ResponseResult articleList(Integer pageNum, Integer pageSize, Long categoryId) {
        //查询条件
        LambdaQueryWrapper<Article> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //如果有categoryId，就要求查询时要和从传入的categoryId相同，如果没有则不需要进行判断
        lambdaQueryWrapper.eq(Objects.nonNull(categoryId) && categoryId > 0, Article::getCategoryId, categoryId);

        //状态是正式发布的
        lambdaQueryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);
        //置顶的文章要放在前面，isTop进行降序排序
        lambdaQueryWrapper.orderByDesc(Article::getIsTop);
        //按发布时间降序排序，越新发布的文章越靠前
        lambdaQueryWrapper.orderByDesc(Article::getCreateTime);
        //分页查询
        Page<Article> page = new Page<>(pageNum, pageSize);
        page(page, lambdaQueryWrapper);

        //查询categoryName
        List<Article> articles = page.getRecords();
        //stream流方式查询
        articles.forEach(article -> {
            article.setCategoryName(categoryService.getById(article.getCategoryId()).getName());
            //从redis中获取viewCount
            Integer viewCount = redisCache.getCacheMapValue(SystemConstants.ARTICLE_VIEW_COUNT, article.getId().toString());
            article.setViewCount(viewCount.longValue());
        });

        //stream流方式查询(方法1)
//        articles.stream()
//                .map(new Function<Article, Article>() {
//                    @Override
//                    public Article apply(Article article) {
//                        //获取分类id，查询分类信息，获取分类名称
//                        Category category = categoryService.getById(article.getCategoryId());
//                        String name = category.getName();
//                        //把分类名称设置给articles
//                        article.setCategoryName(name);
//                        return article;
//                    }
//                });

        //for循环方式查询
//        //再通过articleId去查询articleName
//        for (Article article : articles) {
//            Category category = categoryService.getById(article.getCategoryId());
//            article.setCategoryName(category.getName());
//        }

        //封装查询结果为Vo
        List<ArticleListVo> articleListVos = BeanCopyUtils.copyBeanList(page.getRecords(), ArticleListVo.class);

        PageVo pageVo = new PageVo(articleListVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }

    //查询文章详情
    @Override
    public ResponseResult getArticleDetail(Long id) {
        //根据id查询文章
        Article article = getById(id);
        //从redis中获取viewCount
        Integer viewCount = redisCache.getCacheMapValue(SystemConstants.ARTICLE_VIEW_COUNT, article.getId().toString());
        article.setViewCount(viewCount.longValue());
        //转化成Vo
        ArticleDetailVo articleDetailVo = BeanCopyUtils.copyBean(article, ArticleDetailVo.class);
        
        //根据分类id查询分类名
        Long categoryId = articleDetailVo.getCategoryId();
        Category category = categoryService.getById(categoryId);
        if (category != null) {
            articleDetailVo.setCategoryName(category.getName());//根据id查到了才进行下去防止空指针异常
        }
        
        //根据创建者ID查询作者昵称
        Long createBy = article.getCreateBy();
        String author = "云梦泽"; // 默认作者名
        if (createBy != null) {
            User user = userService.getById(createBy);
            if (user != null && user.getNickname() != null) {
                author = user.getNickname();
            }
        }
        articleDetailVo.setAuthor(author);
        
        //计算字数统计（去除HTML标签后的纯文本字符数）
        String content = article.getContent();
        if (content != null) {
            String textContent = content.replaceAll("<[^>]*>", "").trim();
            int wordCount = textContent.length();
            articleDetailVo.setWordCount(wordCount);
            
            //计算阅读时长（分钟），假设每分钟阅读300字，最小值为1分钟
            int readTime = Math.max(1, (int) Math.ceil(wordCount / 300.0));
            articleDetailVo.setReadTime(readTime);
        } else {
            articleDetailVo.setWordCount(0);
            articleDetailVo.setReadTime(1);
        }
        
        //封装响应返回
        return ResponseResult.okResult(articleDetailVo);
    }

    //根据分类id查询文章列表
    @Override
    public ResponseResult articleListByCategoryId(Long categoryId, Integer pageNum, Integer pageSize) {
        //查询条件
        LambdaQueryWrapper<Article> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据分类id查询
        lambdaQueryWrapper.eq(Article::getCategoryId, categoryId);
        //状态是正式发布的
        lambdaQueryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);
        //置顶的文章要放在前面
        lambdaQueryWrapper.orderByDesc(Article::getIsTop);
        //按发布时间降序排序，越新发布的文章越靠前
        lambdaQueryWrapper.orderByDesc(Article::getCreateTime);
        //分页查询
        Page<Article> page = new Page<>(pageNum, pageSize);
        page(page, lambdaQueryWrapper);

        //查询categoryName
        List<Article> articles = page.getRecords();
        articles.forEach(article -> {
            article.setCategoryName(categoryService.getById(article.getCategoryId()).getName());
            //从redis中获取viewCount
            Integer viewCount = redisCache.getCacheMapValue(SystemConstants.ARTICLE_VIEW_COUNT, article.getId().toString());
            article.setViewCount(viewCount.longValue());
        });

        //封装查询结果为Vo
        List<ArticleListVo> articleListVos = BeanCopyUtils.copyBeanList(page.getRecords(), ArticleListVo.class);

        PageVo pageVo = new PageVo(articleListVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }

    @Override
    public ResponseResult viewCount(Long id)
    {
        // 更新redis中对应文章id的浏览量
        redisCache.incrementCacheMapValue(SystemConstants.ARTICLE_VIEW_COUNT, id.toString(), 1);
        return ResponseResult.okResult();
    }

    //根据标签id查询文章列表
    @Override
    public ResponseResult articleListByTagId(Long tagId, Integer pageNum, Integer pageSize)
    {
        //先通过ArticleTag关联表查询该标签下的所有文章ID
        LambdaQueryWrapper<ArticleTag> articleTagWrapper = new LambdaQueryWrapper<>();
        articleTagWrapper.eq(ArticleTag::getTagId, tagId);
        List<ArticleTag> articleTagList = articleTagMapper.selectList(articleTagWrapper);

        //获取文章ID列表
        List<Long> articleIds = articleTagList.stream()
                .map(ArticleTag::getArticleId)
                .collect(Collectors.toList());

        //查询条件
        LambdaQueryWrapper<Article> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //如果有文章ID，则根据文章ID列表查询；否则查询条件会返回空结果
        if (!articleIds.isEmpty()) {
            lambdaQueryWrapper.in(Article::getId, articleIds);
        } else {
            //没有文章时返回空结果
            lambdaQueryWrapper.eq(Article::getId, -1L);
        }
        //状态是正式发布的
        lambdaQueryWrapper.eq(Article::getStatus, SystemConstants.ARTICLE_STATUS_NORMAL);
        //置顶的文章要放在前面
        lambdaQueryWrapper.orderByDesc(Article::getIsTop);
        //分页查询
        Page<Article> page = new Page<>(pageNum, pageSize);
        page(page, lambdaQueryWrapper);

        //查询categoryName
        List<Article> articles = page.getRecords();
        articles.forEach(article ->
                article.setCategoryName(categoryService.getById(article.getCategoryId()).getName())
        );

        //封装查询结果为Vo
        List<ArticleListVo> articleListVos = BeanCopyUtils.copyBeanList(page.getRecords(), ArticleListVo.class);

        PageVo pageVo = new PageVo(articleListVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }
}

