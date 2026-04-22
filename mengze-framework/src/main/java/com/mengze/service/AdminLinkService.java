package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LinkListDto;
import com.mengze.domain.vo.LinkFormDetailVo;
import com.mengze.domain.vo.PageVo;

/**
 * 后台友链管理服务接口
 *
 * @author mengze
 */
public interface AdminLinkService {

    ResponseResult<PageVo> linkList(Integer pageNum, Integer pageSize, LinkListDto linkListDto);

    ResponseResult<LinkFormDetailVo> linkFormDetail(Long id);
}
