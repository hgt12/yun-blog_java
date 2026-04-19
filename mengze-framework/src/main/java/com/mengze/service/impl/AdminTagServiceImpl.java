package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddTagDto;
import com.mengze.domain.dto.TagListDto;
import com.mengze.domain.dto.UpdateTagDto;
import com.mengze.domain.entity.Article;
import com.mengze.domain.entity.ArticleTag;
import com.mengze.domain.entity.Category;
import com.mengze.domain.entity.Tag;
import com.mengze.domain.vo.AdminTagListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.TagFormDetailVo;
import com.mengze.domain.vo.TagOptionVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.ArticleTagMapper;
import com.mengze.service.AdminTagService;
import com.mengze.service.ArticleService;
import com.mengze.service.TagService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后台标签管理服务实现类
 *
 * @author mengze
 */
@Service
public class AdminTagServiceImpl implements AdminTagService
{

    @Autowired
    private TagService tagService;

    @Autowired
    private ArticleTagMapper articleTagMapper;

    @Autowired
    private ArticleService articleService;

    //获取标签列表
    @Override
    public ResponseResult tagList(Integer pageNum, Integer pageSize, TagListDto tagListDto)
    {
        //构建标签查询条件
        LambdaQueryWrapper<Tag> tagQueryWrapper = new LambdaQueryWrapper<>();

        //根据关键字模糊查询标签名称
        if (StringUtils.hasText(tagListDto.getKeywords())) {
            tagQueryWrapper.like(Tag::getName, tagListDto.getKeywords());
        }

        //只查询未删除的标签
        tagQueryWrapper.eq(Tag::getDelFlag, SystemConstants.NOT_DELETED);

        //按创建时间降序排序
        tagQueryWrapper.orderByDesc(Tag::getCreateTime);

        //分页查询
        Page<Tag> page = new Page<>(pageNum, pageSize);
        tagService.page(page, tagQueryWrapper);

        //查询所有未删除和分类状态正常的文章
        LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
        articleQueryWrapper.eq(Article::getDelFlag, SystemConstants.NOT_DELETED)
                          .select(Article::getId)
                .inSql(Article::getCategoryId,
                        "SELECT id FROM category WHERE status = '0' AND del_flag = '0'");
        List<Article> validArticles = articleService.list(articleQueryWrapper);
        Set<Long> validArticleIds = validArticles.stream()
                .map(Article::getId)
                .collect(Collectors.toSet());

        //查询所有文章标签关联关系
        List<ArticleTag> articleTagList = articleTagMapper.selectList(null);

        //统计每个标签关联的未删除的文章数量
        Map<Long, Long> tagArticleCountMap = articleTagList.stream()
                .filter(articleTag -> validArticleIds.contains(articleTag.getArticleId()))
                .collect(Collectors.groupingBy(ArticleTag::getTagId, Collectors.counting()));

        //封装查询结果为Vo并设置文章数量
        List<AdminTagListVo> tagListVos = page.getRecords().stream()
                .map(tag -> {
                    AdminTagListVo tagListVo = BeanCopyUtils.copyBean(tag, AdminTagListVo.class);
                    tagListVo.setArticleCount(tagArticleCountMap.getOrDefault(tag.getId(), 0L).intValue());
                    return tagListVo;
                })
                .collect(Collectors.toList());

        PageVo pageVo = new PageVo(tagListVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }

    //获取标签下拉选择列表
    @Override
    public ResponseResult getTagOptions()
    {
        //获取标签id
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.eq(Tag::getDelFlag, SystemConstants.NOT_DELETED)
                    .orderByDesc(Tag::getId);

        List<Tag> tags = tagService.list(queryWrapper);

        //封装成Vo
        List<TagOptionVo> tagOptionVos = BeanCopyUtils.copyBeanList(tags, TagOptionVo.class);

        return ResponseResult.okResult(tagOptionVos);
    }

    //获取标签表单数据，用于编辑标签信息
    @Override
    public ResponseResult<TagFormDetailVo> tagFormDetail(Long id)
    {
        //获取标签id，查询标签
        Tag tag = tagService.getById(id);

        //封装成vo
        TagFormDetailVo tagFormDetailVo = BeanCopyUtils.copyBean(tag, TagFormDetailVo.class);

        return ResponseResult.okResult(tagFormDetailVo);
    }

    //添加标签
    @Override
    public ResponseResult addTag(AddTagDto addTagDto)
    {
        //将标签拷贝到Bean再保存标签
        Tag tag = BeanCopyUtils.copyBean(addTagDto, Tag.class);
        tagService.save(tag);

        return ResponseResult.okResult();
    }

    //更新标签
    @Override
    public ResponseResult updateTag(Long id, UpdateTagDto updateTagDto)
    {
        //获取标签id
        updateTagDto.setId(id);

        //更新标签信息
        Tag tag = BeanCopyUtils.copyBean(updateTagDto, Tag.class);
        tagService.updateById(tag);

        return ResponseResult.okResult();
    }

    //批量删除标签 （逻辑删除）
    @Override
    public ResponseResult deleteTag(Long[] ids)
    {
        //获取标签id
        List<Long> tagIdList =List.of(ids);

        //收集所有有文章的标签信息
        StringBuilder errorMsg = new StringBuilder();

        //检查每个标签下是否有文章
        for (Long tagId : tagIdList)
        {
            // 通过article_tag关联表查询该标签关联的文章ID列表
            LambdaQueryWrapper<ArticleTag> articleTagQueryWrapper = new LambdaQueryWrapper<>();
            articleTagQueryWrapper.eq(ArticleTag::getTagId, tagId);
            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagQueryWrapper);

            if (!articleTags.isEmpty())
            {
                // 获取关联的文章ID列表
                List<Long> articleIds = articleTags.stream()
                        .map(ArticleTag::getArticleId)
                        .collect(Collectors.toList());

                // 查询这些文章中未删除的数量
                LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
                articleQueryWrapper.in(Article::getId, articleIds)
                        .eq(Article::getDelFlag, SystemConstants.NOT_DELETED);
                long articleCount = articleService.count(articleQueryWrapper);

                if (articleCount > 0)
                {
                    //如果该标签下有文章，收集错误信息
                    Tag tag = tagService.getById(tagId);
                    String tagName = tag != null ? tag.getName() : "ID:" + tagId;
                    if (!errorMsg.isEmpty()) {
                        errorMsg.append("; ");
                    }
                    errorMsg.append("【").append(tagName).append("】下存在 ").append(articleCount).append(" 篇文章");
                }
            }
        }

        //返回错误信息
        if (!errorMsg.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, errorMsg.toString() + "，无法删除");
        }

        //根据标签id列表删除标签
        tagService.removeByIds(tagIdList);

        return ResponseResult.okResult();
    }
}
