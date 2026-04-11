package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.TagListDto;
import com.mengze.domain.entity.Tag;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.TagVo;
import com.mengze.service.AdminTagService;
import com.mengze.service.TagService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

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

    @Override
    public ResponseResult tagList(Integer pageNum, Integer pageSize, TagListDto tagListDto) {
        //构建查询条件
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();

        //根据标签名称模糊查询
        if (StringUtils.hasText(tagListDto.getName())) {
            queryWrapper.like(Tag::getName, tagListDto.getName());
        }

        //根据备注模糊查询
        if (StringUtils.hasText(tagListDto.getRemark())) {
            queryWrapper.like(Tag::getRemark, tagListDto.getRemark());
        }

        //只查询未删除的标签
        queryWrapper.eq(Tag::getDelFlag, SystemConstants.NOT_DELETED);

        //按创建时间降序排序
        queryWrapper.orderByDesc(Tag::getCreateTime);

        //分页查询
        Page<Tag> page = new Page<>(pageNum, pageSize);
        tagService.page(page, queryWrapper);

        //封装查询结果为Vo
        List<TagVo> tagVos = BeanCopyUtils.copyBeanList(page.getRecords(), TagVo.class);

        PageVo pageVo = new PageVo(tagVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }
}
