package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.SysNotice;
import com.mengze.domain.vo.PageVo;

/**
 * 系统通知公告表(Notice)表服务接口
 *
 * @author makejava
 * @since 2026-05-28 10:19:37
 */
public interface NoticeService extends IService<SysNotice> {

    ResponseResult<PageVo> getNoticeList(Integer pageNum, Integer pageSize);
}

