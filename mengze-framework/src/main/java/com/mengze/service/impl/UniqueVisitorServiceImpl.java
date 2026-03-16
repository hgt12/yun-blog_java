package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.mapper.UniqueVisitorMapper;
import com.mengze.domain.entity.UniqueVisitor;
import com.mengze.service.UniqueVisitorService;
import org.springframework.stereotype.Service;

/**
 * 独立访客记录表(UniqueVisitor)表服务实现类
 *
 * @author makejava
 * @since 2026-03-15 19:12:12
 */
@Service("uniqueVisitorService")
public class UniqueVisitorServiceImpl extends ServiceImpl<UniqueVisitorMapper, UniqueVisitor> implements UniqueVisitorService {

}

