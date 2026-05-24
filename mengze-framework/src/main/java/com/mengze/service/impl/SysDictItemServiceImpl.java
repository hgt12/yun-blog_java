package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.mapper.SysDictItemMapper;
import com.mengze.domain.entity.SysDictItem;
import com.mengze.service.SysDictItemService;
import org.springframework.stereotype.Service;

/**
 * 数据字典项表(SysDictItem)表服务实现类
 *
 * @author makejava
 * @since 2026-05-24 13:59:52
 */
@Service("sysDictItemService")
public class SysDictItemServiceImpl extends ServiceImpl<SysDictItemMapper, SysDictItem> implements SysDictItemService {

}

