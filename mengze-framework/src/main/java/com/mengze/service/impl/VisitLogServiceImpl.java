package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.mapper.VisitLogMapper;
import com.mengze.domain.entity.VisitLog;
import com.mengze.service.VisitLogService;
import org.springframework.stereotype.Service;

/**
 * 访问日志表(VisitLog)表服务实现类
 *
 * @author makejava
 * @since 2026-03-15 19:13:19
 */
@Service("visitLogService")
public class VisitLogServiceImpl extends ServiceImpl<VisitLogMapper, VisitLog> implements VisitLogService {

}

