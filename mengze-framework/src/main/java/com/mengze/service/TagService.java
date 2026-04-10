package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.TagDto;
import com.mengze.domain.entity.Tag;
import com.mengze.domain.vo.PageVo;

/**
 * 标签表(Tag)表服务接口
 *
 * @author makejava
 * @since 2026-01-11 18:59:28
 */
public interface TagService extends IService<Tag> {
    ResponseResult getTagList();

    ResponseResult<PageVo> tagList(Integer pageNum, Integer pageSize, TagDto tagDto);
}

