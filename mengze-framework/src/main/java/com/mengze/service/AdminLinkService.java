package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddLinkDto;
import com.mengze.domain.dto.LinkListDto;
import com.mengze.domain.dto.UpdateLinkDto;
import com.mengze.domain.vo.LinkFormDetailVo;
import com.mengze.domain.vo.PageVo;

/**
 * 后台友链管理服务接口
 *
 * @author mengze
 */
public interface AdminLinkService {

    ResponseResult<PageVo> getLinkList(Integer pageNum, Integer pageSize, LinkListDto linkListDto);

    ResponseResult<LinkFormDetailVo> getLinkFormDetail(Long id);

    ResponseResult addLink(AddLinkDto addLinkDto);

    ResponseResult updateLink(Long id, UpdateLinkDto updateLinkDto);

    ResponseResult deleteLink(Long[] ids);
}
