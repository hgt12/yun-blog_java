package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LogListDto;
import com.mengze.domain.entity.VisitLog;
import com.mengze.domain.vo.PageVo;

/**
 * 访问日志表(VisitLog)表服务接口
 *
 * @author makejava
 * @since 2026-03-15 19:13:19
 */
public interface VisitLogService extends IService<VisitLog> {

    ResponseResult<PageVo> getLogList(Integer pageNum, Integer pageSize, LogListDto logListDto);

    ResponseResult exportLog(Integer pageNum, Integer pageSize, LogListDto logListDto);
}

