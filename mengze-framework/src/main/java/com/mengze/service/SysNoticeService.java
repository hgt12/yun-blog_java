package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddNoticeDto;
import com.mengze.domain.dto.NoticeListDto;
import com.mengze.domain.dto.UpdateNoticeDto;
import com.mengze.domain.entity.SysNotice;
import com.mengze.domain.vo.NoticeFormDetailVo;
import com.mengze.domain.vo.PageVo;

/**
 * 通知公告表(SysNotice)表服务接口
 *
 * @author makejava
 * @since 2026-05-25 20:30:59
 */
public interface SysNoticeService extends IService<SysNotice> {

    ResponseResult<PageVo> getNoticeList(Integer pageNum, Integer pageSize, NoticeListDto noticeListDto);

    ResponseResult<NoticeFormDetailVo> getNoticeFormDetail(Long id);

    ResponseResult addNotice(AddNoticeDto addNoticeDto);

    ResponseResult updateNotice(Long id, UpdateNoticeDto updateNoticeDto);
}

