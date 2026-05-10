package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddPostsDto;
import com.mengze.domain.dto.PostsListDto;
import com.mengze.domain.dto.UpdatePostsDto;
import com.mengze.domain.entity.*;
import com.mengze.domain.vo.*;
import com.mengze.enums.AppHttpCodeEnum;
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
import java.util.Set;
import java.util.stream.Collectors;

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

    @Autowired
    private SysFileService sysFileService;

    //获取文章列表
    @Override
    public ResponseResult getPostsList(Integer pageNum, Integer pageSize, PostsListDto postsListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();

        //根据分类id筛选
        if (Objects.nonNull(postsListDto.getCategoryId()) && postsListDto.getCategoryId() > 0) {
            queryWrapper.eq(Article::getCategoryId, postsListDto.getCategoryId());
        }

        //根据标签id筛选
        if (Objects.nonNull(postsListDto.getTagId()) && postsListDto.getTagId() > 0) {
            //先查询该标签下的所有文章ID
            LambdaQueryWrapper<ArticleTag> articleTagQueryWrapper = new LambdaQueryWrapper<>();
            articleTagQueryWrapper.eq(ArticleTag::getTagId, postsListDto.getTagId());
            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagQueryWrapper);
            
            //提取文章ID列表
            List<Long> articleIds = articleTags.stream()
                    .map(ArticleTag::getArticleId)
                    .collect(Collectors.toList());

            //如果该标签下没有文章，返回空结果
            if (articleIds.isEmpty()) {
                return ResponseResult.okResult(new PageVo(List.of(), 0L));
            }

            //根据文章ID列表筛选
            queryWrapper.in(Article::getId, articleIds);
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

        //填充分类名称、分类状态和文章浏览量
        List<Article> articles = page.getRecords();
        articles.forEach(article -> {
            Category category = categoryService.getById(article.getCategoryId());
            article.setCategoryName(category.getName());
            article.setCategoryStatus(category.getStatus());
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
    public ResponseResult<PostsFormDetailVo> getPostsDetail(Long id)
    {
        //先查询文章id
        Article article = articleService.getById(id);
        if (article == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该文章不存在！");
        }

        //转化成Vo
        PostsFormDetailVo postsFormDetailVo = BeanCopyUtils.copyBean(article, PostsFormDetailVo.class);

        //填充分类名称
        postsFormDetailVo.setCategoryName(categoryService.getById(article.getCategoryId()).getName());

        //填充标签ID列表
        List<Long> tags = getTagIdsByArticleId(id);
        postsFormDetailVo.setTags(tags);

        return ResponseResult.okResult(postsFormDetailVo);
    }

    //添加文章/写博客
    @Override
    @Transactional
    public ResponseResult addPosts(AddPostsDto postsDto)
    {

        //处理文章内容中的临时图片，将其转为正式文件
        String content = postsDto.getContent();
        if (ImageUrlUtils.containsTempImages(content)) {
            content = processContentImagesAndSaveRecords(content, postsDto.getTitle());
            postsDto.setContent(content);
        }

        //处理缩略图
        String thumbnail = postsDto.getThumbnail();
        if (StringUtils.hasText(thumbnail) && thumbnail.contains("temp/")) {
            UploadFileMetaVo uploadFileMetaVo = ossFileService.moveTempToFormal(thumbnail);
            //判断图片是否保存（从oss中的 临时 > 正式）成功
            if (uploadFileMetaVo == null || !StringUtils.hasText(uploadFileMetaVo.getUrl())) {
                throw new RuntimeException("缩略图保存失败，无法保存文章记录: " + thumbnail);
            }
            String fileSource = "文章《" + postsDto.getTitle() + "》中的缩略图";
            thumbnail = uploadFileMetaVo.getUrl();
            postsDto.setThumbnail(thumbnail);
            saveFileRecord(uploadFileMetaVo.getName(),
                    uploadFileMetaVo.getFilePath(),
                    uploadFileMetaVo.getUrl(),
                    uploadFileMetaVo.getSize(),
                    uploadFileMetaVo.getMimeType(),
                    fileSource);
        }

        //添加文章
        Article article = BeanCopyUtils.copyBean(postsDto, Article.class);
        articleService.save(article);

        //初始化Redis中的浏览量为0
        redisCache.setCacheMapValue(SystemConstants.ARTICLE_VIEW_COUNT, article.getId().toString(), 0);

        List<ArticleTag> articleTags = postsDto.getTags().stream()
                .map(tagId -> new ArticleTag(article.getId(), tagId))
                .collect(Collectors.toList());

        //添加博客和标签的关联关系
        articleTagService.saveBatch(articleTags);
        return ResponseResult.okResult();
    }

    //编辑/更新文章
    @Override
    @Transactional
    public ResponseResult updatePosts(Long id, UpdatePostsDto updatePostsDto)
    {
        // 获取ID
        updatePostsDto.setId(id);

        //检查文章是否存在
        Article oldArticle = articleService.getById(id);
        if (oldArticle == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该文章不存在！");
        }

        // 处理文章内容中的图片
        String content = updatePostsDto.getContent();
        List<String> oldContentImages = new ArrayList<>();
        List<String> newContentImages = new ArrayList<>();

        // 提取旧内容中的图片URL
        if (StringUtils.hasText(oldArticle.getContent())) {
            oldContentImages = ImageUrlUtils.extractImageUrls(oldArticle.getContent());
        }

        // 处理新内容中的临时图片
        if (ImageUrlUtils.containsTempImages(content)) {
            content = processContentImagesAndSaveRecords(content, oldArticle.getTitle());
            updatePostsDto.setContent(content);
        }

        // 提取新内容中的图片URL
        if (StringUtils.hasText(content)) {
            newContentImages = ImageUrlUtils.extractImageUrls(content);
        }

        // 删除不再使用的内容图片
        for (String oldImageUrl : oldContentImages) {
            if (!newContentImages.contains(oldImageUrl)) {
                ossFileService.deleteFileByUrl(oldImageUrl);
                deleteFileRecordByUrl(oldImageUrl);
                log.info("删除旧的内容图片并同步文件表: {}", oldImageUrl);
            }
        }

        // 处理缩略图
        String newThumbnail = updatePostsDto.getThumbnail();
        String oldThumbnail = oldArticle.getThumbnail();

        if (StringUtils.hasText(newThumbnail) && newThumbnail.contains("temp/")) {
            UploadFileMetaVo uploadFileMetaVo = ossFileService.moveTempToFormal(newThumbnail);
            if (uploadFileMetaVo == null || !StringUtils.hasText(uploadFileMetaVo.getUrl())) {
                throw new RuntimeException("缩略图保存失败，无法更新文章记录: " + newThumbnail);
            }
            newThumbnail = uploadFileMetaVo.getUrl();
            updatePostsDto.setThumbnail(newThumbnail);
            saveFileRecord(uploadFileMetaVo.getName(),
                    uploadFileMetaVo.getFilePath(),
                    uploadFileMetaVo.getUrl(),
                    uploadFileMetaVo.getSize(),
                    uploadFileMetaVo.getMimeType(),
                    "文章《" + oldArticle.getTitle() + "》中的缩略图");
        }

        // 如果缩略图发生变化，删除旧缩略图
        if (StringUtils.hasText(oldThumbnail) &&
            StringUtils.hasText(newThumbnail) &&
            !oldThumbnail.equals(newThumbnail)) {
            ossFileService.deleteFileByUrl(oldThumbnail);
            deleteFileRecordByUrl(oldThumbnail);
            log.info("删除旧的缩略图并同步文件表: {}", oldThumbnail);
        }

        // 更新文章基本信息
        Article article = BeanCopyUtils.copyBean(updatePostsDto, Article.class);
        articleService.updateById(article);

        // 更新标签关联关系
        // 先删除旧的关联关系
        LambdaQueryWrapper<ArticleTag> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ArticleTag::getArticleId, updatePostsDto.getId());
        articleTagMapper.delete(deleteWrapper);

        // 添加新的关联关系
        if (updatePostsDto.getTags() != null && !updatePostsDto.getTags().isEmpty()) {
            List<ArticleTag> articleTags = updatePostsDto.getTags().stream()
                    .map(tagId -> new ArticleTag(article.getId(), tagId))
                    .collect(Collectors.toList());
            articleTagService.saveBatch(articleTags);
        }

        return ResponseResult.okResult();
    }

    // 批量删除文章（逻辑删除），将文章中的图片有的图片从oss里的正式文件夹转移到 deleted 文件夹中
    // 如果后续需要有恢复文章的功能，则可以利用逻辑删除这个特性，将文章恢复并且将图片或文件从 deleted 文件夹移动到正式文件夹中
    @Override
    @Transactional
    public ResponseResult deletePosts(Long[] ids)
    {
        // 参数校验
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的文章");
        }

        List<Long> idList = List.of(ids);

        //检查文章是否存在
        List<Article> articlesToDelete = articleService.listByIds(idList);
        if (articlesToDelete.size() != ids.length) {
            //找出不存在的 文章id
            Set<Long> existIds = articlesToDelete.stream()
                    .map(Article::getId)
                    .collect(Collectors.toSet());

            List<Long> notExistIds = idList.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败！！原因：以下文章 id 不存在：" + notExistIds);
        }

        // 遍历每个文章ID，将相关图片移动到deleted文件夹，并同步删除文件表记录
        for (Long articleId : idList)
        {
            Article article = articleService.getById(articleId);

            // 查找需要移动的图片URL
            List<String> imageUrls = new ArrayList<>();
            
            // 查找文章内容中的图片
            if (StringUtils.hasText(article.getContent())) {
                List<String> contentImages = ImageUrlUtils.extractImageUrls(article.getContent());
                imageUrls.addAll(contentImages);
            }

            // 查找缩略图
            if (StringUtils.hasText(article.getThumbnail())) {
                imageUrls.add(article.getThumbnail());
            }
            
            // 批量移动图片到 deleted 文件夹
            if (!imageUrls.isEmpty()) {
                int movedCount = ossFileService.batchMoveFilesToDeleted(imageUrls);
                log.info("文章ID: {} 的图片已移动到deleted文件夹，成功: {}/{}", 
                    articleId, movedCount, imageUrls.size());

                // 同步删除文件表记录
                for (String imageUrl : imageUrls) {
                    deleteFileRecordByUrl(imageUrl);
                }
                log.info("文章ID: {} 的图片文件表记录已删除，共 {} 条", articleId, imageUrls.size());
            }

            // 删除文章标签关联关系
            LambdaQueryWrapper<ArticleTag> deleteWrapper = new LambdaQueryWrapper<>();
            deleteWrapper.eq(ArticleTag::getArticleId, articleId);
            int deletedCount = articleTagMapper.delete(deleteWrapper);
            log.info("删除文章ID: {} 的标签关联记录，共 {} 条", articleId, deletedCount);
        }

        // 逻辑删除文章（将 del_flag 从 0 > 1）
        articleService.removeByIds(idList);
        log.info("批量逻辑删除文章成功，ID列表: {}", idList);

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
                    tagVo.setStatus(tag.getStatus());
                    tagVo.setCreateTime(tag.getCreateTime());
                    LambdaQueryWrapper<ArticleTag> countWrapper = new LambdaQueryWrapper<>();
                    countWrapper.eq(ArticleTag::getTagId, tag.getId());
                    Long count = articleTagMapper.selectCount(countWrapper);
                    tagVo.setArticleCount(count.intValue());
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

    //处理文章内容中的图片并将其写入文件表
    private String processContentImagesAndSaveRecords(String content, String postTitle)
    {
        List<String> tempImageUrls = ImageUrlUtils.extractTempImageUrls(content);
        String result = content;

        // 因为同一篇文章里可能重复引用同一张临时图片，所一这里先去重，避免重复转正和重复删除临时文件
        List<String> distinctTempImageUrls = tempImageUrls.stream().distinct().collect(Collectors.toList());

        for (String tempUrl : distinctTempImageUrls) {
            UploadFileMetaVo uploadFileMetaVo = ossFileService.moveTempToFormal(tempUrl);

            //判断图片是否保存（从oss中的 临时 > 正式）成功
            if (uploadFileMetaVo == null || !StringUtils.hasText(uploadFileMetaVo.getUrl())) {
                throw new RuntimeException("图片保存失败，无法保存文章内容中的图片记录: " + tempUrl);
            }

            result = ImageUrlUtils.replaceImageUrl(result, tempUrl, uploadFileMetaVo.getUrl());
            if (StringUtils.hasText(uploadFileMetaVo.getFilePath())) {
                String fileSource = "文章《" + postTitle + "》中的图片";
                saveFileRecord(uploadFileMetaVo.getName(),
                        uploadFileMetaVo.getFilePath(),
                        uploadFileMetaVo.getUrl(),
                        uploadFileMetaVo.getSize(),
                        uploadFileMetaVo.getMimeType(),
                        fileSource);
            }
        }

        return result;
    }

    //将元数据写入文件表
    private void saveFileRecord(String fileName, String filePath, String url, Long size, String mimeType, String fileSource)
    {
        SysFile sysFile = new SysFile();
        sysFile.setName(fileName);
        sysFile.setFilePath(filePath);
        sysFile.setUrl(url);
        sysFile.setSize(size);
        sysFile.setMimeType(mimeType);
        sysFile.setFileSource(fileSource);
        sysFile.setType(SystemConstants.FILE_TYPE_IMAGE);
        sysFileService.save(sysFile);
    }

    //处理删除文件
    private void deleteFileRecordByUrl(String url)
    {
        if (!StringUtils.hasText(url)) {
            return;
        }

        LambdaQueryWrapper<SysFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysFile::getUrl, url);
        sysFileService.remove(queryWrapper);
    }
}
