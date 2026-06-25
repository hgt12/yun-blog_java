package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.RedisKeyConstants;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.*;
import com.mengze.domain.entity.SysUser;
import com.mengze.domain.vo.ArticleDetailVo;
import com.mengze.domain.vo.ArticleListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.TagVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.ArticleMapper;
import com.mengze.mapper.ArticleTagMapper;
import com.mengze.mapper.TagMapper;
import com.mengze.service.ArticleService;
import com.mengze.service.CategoryService;
import com.mengze.service.SysUserService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文章表(Article)表服务实现类
 *
 * @author makejava
 * @since 2025-11-22 15:43:53
 */
@Service("articleService")
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService
{

    @Autowired
    @Lazy
    private CategoryService categoryService;

    @Autowired
    @Lazy
    private SysUserService sysUserService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private TagMapper tagMapper;

    //分页查询文章列表
    @Override
    public ResponseResult articleList(Integer pageNum, Integer pageSize)
    {
        //查询条件
        LambdaQueryWrapper<Article> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //只查询分类状态正常的文章（使用子查询）
        lambdaQueryWrapper.inSql(Article::getCategoryId, 
                "SELECT id FROM category WHERE status = '0' AND del_flag = '0'");
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
            //读取数据库中的文章预览量
            Article viewCountArticle = getById(article.getId());
            article.setViewCount(viewCountArticle != null && viewCountArticle.getViewCount() != null ? viewCountArticle.getViewCount() : 0L);
        });

        //封装查询结果为Vo
        List<ArticleListVo> articleListVos = BeanCopyUtils.copyBeanList(page.getRecords(), ArticleListVo.class);
        
        //获取标签
        fillArticleListTags(articleListVos);

        PageVo pageVo = new PageVo(articleListVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }

    //查询文章详情
    @Override
    public ResponseResult getArticleDetail(Long id)
    {
        //根据id查询文章
        Article article = getById(id);
        if (article == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该文章不存在！");
        }
        //读取数据库中的文章预览量
        Article viewCountArticle = getById(article.getId());
        article.setViewCount(viewCountArticle != null && viewCountArticle.getViewCount() != null ? viewCountArticle.getViewCount() : 0L);
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
        String author = SystemConstants.DEFAULT_AUTHOR; // 默认作者名
        if (createBy != null) {
            SysUser sysUser = sysUserService.getById(createBy);
            if (sysUser != null && sysUser.getNickname() != null) {
                author = sysUser.getNickname();
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
        
        //获取标签数据
        fillArticleDetailTags(articleDetailVo);
        
        //封装响应返回
        return ResponseResult.okResult(articleDetailVo);
    }

    //根据分类id查询文章列表
    @Override
    public ResponseResult articleListByCategoryId(Long categoryId, Integer pageNum, Integer pageSize)
    {
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
            //读取数据库中的文章预览量
            Article viewCountArticle = getById(article.getId());
            article.setViewCount(viewCountArticle != null && viewCountArticle.getViewCount() != null ? viewCountArticle.getViewCount() : 0L);
        });

        //封装查询结果为Vo
        List<ArticleListVo> articleListVos = BeanCopyUtils.copyBeanList(page.getRecords(), ArticleListVo.class);

        PageVo pageVo = new PageVo(articleListVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }

    //更新文章预览增量
    @Override
    public ResponseResult viewCount(Long id)
    {
        // 更新redis中对应文章id的浏览量增量
        redisCache.incrementCacheMapValue(RedisKeyConstants.ARTICLE_VIEW_COUNT_INCREMENT_KEY, id.toString(), 1);
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
        //只查询分类状态正常的文章（使用子查询）
        lambdaQueryWrapper.inSql(Article::getCategoryId, 
                "SELECT id FROM category WHERE status = '0' AND del_flag = '0'");
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

    /**
     * 为文章列表添加标签数据
     * @param articleListVos 文章列表VO
     */
    private void fillArticleListTags(List<ArticleListVo> articleListVos)
    {
        //收集所有文章ID
        List<Long> articleIds = articleListVos.stream()
                .map(ArticleListVo::getId)
                .collect(Collectors.toList());

        //查询这些文章的标签关联
        LambdaQueryWrapper<ArticleTag> articleTagWrapper = new LambdaQueryWrapper<>();
        if (!articleIds.isEmpty()) {
            articleTagWrapper.in(ArticleTag::getArticleId, articleIds);
        }
        List<ArticleTag> articleTagList = articleTagMapper.selectList(articleTagWrapper);

        //收集所有标签ID
        List<Long> tagIds = articleTagList.stream()
                .map(ArticleTag::getTagId)
                .distinct()
                .collect(Collectors.toList());

        //查询标签详情
        LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
        if (!tagIds.isEmpty()) {
            tagWrapper.in(Tag::getId, tagIds)
                    .eq(Tag::getStatus, SystemConstants.STATUS_NORMAL);
        }
        List<Tag> tags = tagMapper.selectList(tagWrapper);

        //转换为TagVo的Map，key为tagId
        Map<Long, TagVo> tagVoMap = tags.stream()
                .collect(Collectors.toMap(
                        Tag::getId,
                        tag -> BeanCopyUtils.copyBean(tag, TagVo.class)
                ));

        //构建文章ID到标签列表的映射
        //为每篇文章创建独立的TagVo对象，避免FastJson的循环引用检测
        Map<Long, List<TagVo>> articleTagMap = articleTagList.stream()
                .filter(at -> tagVoMap.containsKey(at.getTagId()))
                .collect(Collectors.groupingBy(
                        ArticleTag::getArticleId,
                        Collectors.mapping(
                                at -> BeanCopyUtils.copyBean(tagVoMap.get(at.getTagId()), TagVo.class),
                                Collectors.toList()
                        )
                ));

        //为每篇文章设置标签列表
        articleListVos.forEach(article ->
                article.setTags(articleTagMap.getOrDefault(article.getId(), List.of()))
        );
    }

    /**
     * 为文章详情添加标签数据
     * @param articleDetailVo 文章详情VO
     */
    private void fillArticleDetailTags(ArticleDetailVo articleDetailVo)
    {
        //查询该文章的标签关联
        LambdaQueryWrapper<ArticleTag> articleTagWrapper = new LambdaQueryWrapper<>();
        articleTagWrapper.eq(ArticleTag::getArticleId, articleDetailVo.getId());
        List<ArticleTag> articleTagList = articleTagMapper.selectList(articleTagWrapper);

        //收集标签ID
        List<Long> tagIds = articleTagList.stream()
                .map(ArticleTag::getTagId)
                .collect(Collectors.toList());

        //查询标签详情
        LambdaQueryWrapper<Tag> tagWrapper = new LambdaQueryWrapper<>();
        if (!tagIds.isEmpty()) {
            tagWrapper.in(Tag::getId, tagIds)
                    .eq(Tag::getStatus, SystemConstants.STATUS_NORMAL);
        }
        List<Tag> tags = tagMapper.selectList(tagWrapper);

        //转换为TagVo的Map，key为tagId
        Map<Long, TagVo> tagVoMap = tags.stream()
                .collect(Collectors.toMap(
                        Tag::getId,
                        tag -> BeanCopyUtils.copyBean(tag, TagVo.class)
                ));

        //填充标签信息
        List<TagVo> tagVos = articleTagList.stream()
                .map(ArticleTag::getTagId)
                .filter(tagVoMap::containsKey)
                .map(tagVoMap::get)
                .collect(Collectors.toList());

        articleDetailVo.setTags(tagVos);
    }
}

