package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LogListDto;
import com.mengze.domain.vo.VisitLogListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.mapper.VisitLogMapper;
import com.mengze.domain.entity.VisitLog;
import com.mengze.service.VisitLogService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 访问日志表(VisitLog)表服务实现类
 *
 * @author makejava
 * @since 2026-03-15 19:13:19
 */
@Service("visitLogService")
public class VisitLogServiceImpl extends ServiceImpl<VisitLogMapper, VisitLog> implements VisitLogService
{
    @Autowired
    private VisitLogService visitLogService;

    //获取日志列表
    @Override
    public ResponseResult<PageVo> getLogList(Integer pageNum, Integer pageSize, LogListDto logListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<VisitLog> queryWrapper = new LambdaQueryWrapper<>();

        //根据关键字模糊查询操作人或页面标题
        if (StringUtils.hasText(logListDto.getKeywords())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(VisitLog::getOperator, logListDto.getKeywords())
                    .or()
                    .like(VisitLog::getPageTitle, logListDto.getKeywords())
            );
        }

        //根据访问时间降序排序
        queryWrapper.orderByDesc(VisitLog::getVisitTime);

        //封装分页
        Page<VisitLog> page = new Page<>(pageNum, pageSize);
        visitLogService.page(page, queryWrapper);

        //封装成vo返回
        List<VisitLogListVo> visitLogListVos = BeanCopyUtils.copyBeanList(page.getRecords(), VisitLogListVo.class);
        PageVo pageVo = new PageVo(visitLogListVos, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }
}

