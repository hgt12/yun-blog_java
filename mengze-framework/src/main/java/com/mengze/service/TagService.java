package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Tag;

/**
 * 标签表(Tag)表服务接口
 *
 * @author makejava
 * @since 2026-01-11 18:59:28
 */
public interface TagService extends IService<Tag> {
    ResponseResult getTagList();
}

