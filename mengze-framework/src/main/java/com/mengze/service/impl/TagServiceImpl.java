package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.mapper.TagMapper;
import com.mengze.domain.entity.Tag;
import com.mengze.service.TagService;
import org.springframework.stereotype.Service;

/**
 * 标签表(Tag)表服务实现类
 *
 * @author makejava
 * @since 2026-01-11 18:59:28
 */
@Service("tagService")
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public ResponseResult getTagList() {
        return null;
    }
}

