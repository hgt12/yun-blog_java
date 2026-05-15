package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.mapper.SysDeptMapper;
import com.mengze.domain.entity.SysDept;
import com.mengze.service.SysDeptService;
import org.springframework.stereotype.Service;

/**
 * 部门表(SysDept)表服务实现类
 *
 * @author makejava
 * @since 2026-05-15 13:06:17
 */
@Service("sysDeptService")
public class SysDeptServiceImpl extends ServiceImpl<SysDeptMapper, SysDept> implements SysDeptService {

}

