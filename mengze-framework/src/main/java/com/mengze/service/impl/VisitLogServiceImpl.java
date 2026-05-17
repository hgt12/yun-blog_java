package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LogListDto;
import com.mengze.domain.vo.VisitLogExportVo;
import com.mengze.domain.vo.VisitLogListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.mapper.VisitLogMapper;
import com.mengze.domain.entity.VisitLog;
import com.mengze.service.VisitLogService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    @Autowired
    private VisitLogMapper visitLogMapper;

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

        //按访问时间查询
        if (logListDto.getVisitTime() != null && logListDto.getVisitTime().size() == 2)
        {
            //补全当天完整时间，因为前端只传 2026-04-01 不完整
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Date startTime = sdf.parse(logListDto.getVisitTime().get(0) + " 00:00:00");
                Date endTime = sdf.parse(logListDto.getVisitTime().get(1) + " 23:59:59");

                queryWrapper.between(VisitLog::getVisitTime, startTime, endTime);
            } catch (ParseException e) {
                throw new RuntimeException("访问时间格式不正确", e);
            }
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

    //导出日志
    @Override
    public ResponseResult exportLog(Integer pageNum, Integer pageSize, LogListDto logListDto)
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

        //按访问时间查询
        if (logListDto.getVisitTime() != null && logListDto.getVisitTime().size() == 2)
        {
            //补全当天完整时间，因为前端只传 2026-04-01 不完整
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                Date startTime = sdf.parse(logListDto.getVisitTime().get(0) + " 00:00:00");
                Date endTime = sdf.parse(logListDto.getVisitTime().get(1) + " 23:59:59");

                queryWrapper.between(VisitLog::getVisitTime, startTime, endTime);
            } catch (ParseException e) {
                throw new RuntimeException("访问时间格式不正确", e);
            }
        }

        //根据访问时间降序排序
        queryWrapper.orderByDesc(VisitLog::getVisitTime);

        //根据是否传入分页参数决定查询方式
        List<VisitLog> logs;
        if (pageNum != null && pageSize != null && pageNum > 0 && pageSize > 0) {
            //分页查询
            Page<VisitLog> page = new Page<>(pageNum, pageSize);
            visitLogMapper.selectPage(page, queryWrapper);
            logs = page.getRecords();
        } else {
            //查询所有符合条件的评论
            logs = visitLogMapper.selectList(queryWrapper);
        }

        //转换为导出VO
        List<VisitLogExportVo> exportData;
        if (logs == null || logs.isEmpty()) {
            exportData = new ArrayList<>();
        } else {
            exportData = BeanCopyUtils.copyBeanList(logs, VisitLogExportVo.class);
        }

        return ResponseResult.okResult(exportData);
    }
}

