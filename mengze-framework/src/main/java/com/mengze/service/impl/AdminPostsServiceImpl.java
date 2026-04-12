package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddPostsDto;
import com.mengze.domain.dto.PostsListDto;
import com.mengze.domain.entity.Article;
import com.mengze.domain.vo.*;
import com.mengze.domain.entity.ArticleTag;
import com.mengze.domain.entity.Tag;
import com.mengze.mapper.ArticleTagMapper;
import com.mengze.service.*;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.ImageUrlUtils;
import com.mengze.utils.RedisCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.extension.toolkit.Db.save;

/**
 * 后台文章管理服务实现类
 *
 * @author mengze
 */
@Service
@Slf4j
public class AdminPostsServiceImpl implements AdminPostsService
{

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private ArticleTagService articleTagService;

    @Autowired
    private TagService tagService;
    
    @Autowired
    private OssFileService ossFileService;

    //获取文章列表
    @Override
    public ResponseResult postsList(Integer pageNum, Integer pageSize, PostsListDto postsListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();

        //根据分类id筛选
        if (Objects.nonNull(postsListDto.getCategoryId()) && postsListDto.getCategoryId() > 0) {
            queryWrapper.eq(Article::getCategoryId, postsListDto.getCategoryId());
        }

        //根据状态筛选
        if (StringUtils.hasText(postsListDto.getStatus())) {
            queryWrapper.eq(Article::getStatus, postsListDto.getStatus());
        }

        //根据关键词搜索（标题或摘要）
        if (StringUtils.hasText(postsListDto.getKeywords())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Article::getTitle, postsListDto.getKeywords())
                    .or()
                    .like(Article::getSummary, postsListDto.getKeywords())
            );
        }

        //置顶的文章要放在前面
        queryWrapper.orderByDesc(Article::getIsTop);
        //按创建时间降序排序
        queryWrapper.orderByDesc(Article::getCreateTime);

        //分页查询
        Page<Article> page = new Page<>(pageNum, pageSize);
        articleService.page(page, queryWrapper);

        //填充分类名称和浏览量
        List<Article> articles = page.getRecords();
        articles.forEach(article -> {
            article.setCategoryName(categoryService.getById(article.getCategoryId()).getName());
            //从redis中获取viewCount
            Integer viewCount = redisCache.getCacheMapValue(SystemConstants.ARTICLE_VIEW_COUNT, article.getId().toString());
            article.setViewCount(viewCount.longValue());
        });

        //封装查询结果为Vo
        List<AdminPostsListVo> postsListVos = BeanCopyUtils.copyBeanList(page.getRecords(), AdminPostsListVo.class);

        //填充标签信息
        postsListVos.forEach(postsListVo -> {
            List<TagVo> tags = getTagsByArticleId(postsListVo.getId());
            postsListVo.setTags(tags);
        });

        PageVo pageVo = new PageVo(postsListVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }

    //获取文章详情信息
    @Override
    public ResponseResult<PostsFormDetailVo> postsDetail(Long id)
    {
        //先查询文章id
        Article article = articleService.getById(id);

        //转化成Vo
        PostsFormDetailVo postsFormDetailVo = BeanCopyUtils.copyBean(article, PostsFormDetailVo.class);

        //填充分类名称
        postsFormDetailVo.setCategoryName(categoryService.getById(article.getCategoryId()).getName());

        //填充标签ID列表
        List<Long> tagIds = getTagIdsByArticleId(id);
        postsFormDetailVo.setTags(tagIds);

        return ResponseResult.okResult(postsFormDetailVo);
    }

    //添加文章/写博客
    @Override
    @Transactional
    public ResponseResult addPosts(AddPostsDto postsDto)
    {
        //添加文章
        Article article = BeanCopyUtils.copyBean(postsDto, Article.class);
        save(article);

        //初始化Redis中的浏览量为0
        redisCache.setCacheMapValue(SystemConstants.ARTICLE_VIEW_COUNT, article.getId().toString(), 0);

        List<ArticleTag> articleTags = postsDto.getTags().stream()
                .map(tagId -> new ArticleTag(article.getId(), tagId))
                .collect(Collectors.toList());

        //添加博客和标签的关联关系
        articleTagService.saveBatch(articleTags);
        return ResponseResult.okResult();
    }
    

    
    /**
     * 根据文章ID查询标签列表
     * @param articleId 文章ID
     * @return 标签列表
     */
    private List<TagVo> getTagsByArticleId(Long articleId)
    {
        //查询文章标签关联表
        LambdaQueryWrapper<ArticleTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(queryWrapper);

        //获取标签ID列表
        List<Long> tagIds = articleTags.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toList());

        //如果没有标签，返回空列表
        if (tagIds.isEmpty()) {
            return List.of();
        }

        //查询标签信息
        List<Tag> tags = tagService.listByIds(tagIds);

        //转换为TagVo
        return tags.stream()
                .map(tag -> {
                    TagVo tagVo = new TagVo();
                    tagVo.setId(tag.getId());
                    tagVo.setName(tag.getName());
                    tagVo.setCreateTime(tag.getCreateTime());
                    return tagVo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据文章ID查询标签ID列表
     * @param articleId 文章ID
     * @return 标签ID列表
     */
    private List<Long> getTagIdsByArticleId(Long articleId)
    {
        //查询文章标签关联表
        LambdaQueryWrapper<ArticleTag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleTag::getArticleId, articleId);
        List<ArticleTag> articleTags = articleTagMapper.selectList(queryWrapper);

        //返回标签ID列表
        return articleTags.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toList());
    }
}
