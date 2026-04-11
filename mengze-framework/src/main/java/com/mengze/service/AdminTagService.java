package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.TagListDto;

/**
 * 后台标签管理服务接口
 *
 * @author mengze
 */
public interface AdminTagService {

    ResponseResult tagList(Integer pageNum, Integer pageSize, TagListDto tagListDto);

}
