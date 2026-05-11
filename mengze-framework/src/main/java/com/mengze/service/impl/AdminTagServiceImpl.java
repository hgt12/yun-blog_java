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
    public ResponseResult getTagList(Integer pageNum, Integer pageSize, TagListDto tagListDto)
    {
        //构建标签查询条件
        LambdaQueryWrapper<Tag> tagQueryWrapper = new LambdaQueryWrapper<>();

        //根据关键字模糊查询标签名称
        if (StringUtils.hasText(tagListDto.getKeywords())) {
            tagQueryWrapper.like(Tag::getName, tagListDto.getKeywords());
        }

        //根据状态筛选
        if (StringUtils.hasText(tagListDto.getStatus())) {
            tagQueryWrapper.eq(Tag::getStatus, tagListDto.getStatus());
        }

        //先以排序序号升序排序和序号相同时按创建时间降序排序
        tagQueryWrapper.orderByAsc(Tag::getSort)
                       .orderByDesc(Tag::getCreateTime);

        //分页查询
        Page<Tag> page = new Page<>(pageNum, pageSize);
        tagService.page(page, tagQueryWrapper);

        //查询所有文章
        LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
        articleQueryWrapper.select(Article::getId);
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

        //查询状态正常的标签
        queryWrapper.eq(Tag::getStatus, SystemConstants.STATUS_NORMAL)
                    .orderByDesc(Tag::getId);

        List<Tag> tags = tagService.list(queryWrapper);

        //封装成Vo
        List<TagOptionVo> tagOptionVos = BeanCopyUtils.copyBeanList(tags, TagOptionVo.class);

        return ResponseResult.okResult(tagOptionVos);
    }

    //获取标签表单数据，用于编辑标签信息
    @Override
    public ResponseResult<TagFormDetailVo> getTagFormDetail(Long id)
    {
        //获取标签id，查询标签
        Tag tag = tagService.getById(id);
        if (tag == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该标签不存在！");
        }

        //封装成vo
        TagFormDetailVo tagFormDetailVo = BeanCopyUtils.copyBean(tag, TagFormDetailVo.class);

        return ResponseResult.okResult(tagFormDetailVo);
    }

    //添加标签
    @Override
    public ResponseResult addTag(AddTagDto addTagDto)
    {
        // 检查标签名称是否已存在
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getName, addTagDto.getName());
        Long count = tagService.count(queryWrapper);
        
        if (count > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "标签名称已存在，请勿重复添加！");
        }
        
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

        //检查标签是否存在
        Tag oldTag = tagService.getById(id);
        if (oldTag == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该标签不存在！");
        }

        // 检查标签名称是否与其他标签重复（排除自己）
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Tag::getName, updateTagDto.getName())
                    .ne(Tag::getId, id); // 排除当前标签
        long count = tagService.count(queryWrapper);

        if (count > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "标签名称已存在，请修改后重试！");
        }

        //更新标签信息
        Tag tag = BeanCopyUtils.copyBean(updateTagDto, Tag.class);
        tagService.updateById(tag);

        return ResponseResult.okResult();
    }

    //批量删除标签 （逻辑删除）
    @Override
    public ResponseResult deleteTag(Long[] ids)
    {
        // 参数校验
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的标签");
        }

        //获取标签id
        List<Long> tagIdList =List.of(ids);

        //检查标签是否存在
        List<Tag> tagsToDelete = tagService.listByIds(tagIdList);
        if (tagsToDelete.size() != ids.length) {
            //找出不存在的 标签id
            Set<Long> existIds = tagsToDelete.stream()
                    .map(Tag::getId)
                    .collect(Collectors.toSet());

            List<Long> notExistIds = tagIdList.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败！！原因：以下标签 id 不存在：" + notExistIds);
        }

        StringBuilder errorMsg = new StringBuilder();

        //收集所有有文章的标签信息并检查每个标签下是否有文章
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

                // 查询文章数量
                LambdaQueryWrapper<Article> articleQueryWrapper = new LambdaQueryWrapper<>();
                if (!articleIds.isEmpty()) {
                    articleQueryWrapper.in(Article::getId, articleIds);
                }
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
