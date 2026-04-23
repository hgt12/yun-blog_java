package com.mengze.service;

import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddTagDto;
import com.mengze.domain.dto.TagListDto;
import com.mengze.domain.dto.UpdateTagDto;
import com.mengze.domain.vo.TagFormDetailVo;

/**
 * 后台标签管理服务接口
 *
 * @author mengze
 */
public interface AdminTagService {

    ResponseResult getTagList(Integer pageNum, Integer pageSize, TagListDto tagListDto);

    ResponseResult getTagOptions();

    ResponseResult<TagFormDetailVo> getTagFormDetail(Long id);

    ResponseResult addTag(AddTagDto addTagDto);

    ResponseResult updateTag(Long id, UpdateTagDto updateTagDto);

    ResponseResult deleteTag(Long[] ids);
}
