package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddDictItemDto;
import com.mengze.domain.dto.DictItemListDto;
import com.mengze.domain.dto.UpdateDictItemDto;
import com.mengze.domain.entity.SysDictItem;
import com.mengze.domain.vo.DictItemFormDetailVo;

import javax.validation.Valid;

/**
 * 数据字典项表(SysDictItem)表服务接口
 *
 * @author makejava
 * @since 2026-05-24 13:59:52
 */
public interface SysDictItemService extends IService<SysDictItem> {

    ResponseResult getDictItemList(Integer pageNum, Integer pageSize, String dictCode, DictItemListDto dictItemListDto);

    ResponseResult getDictItemOptions(String dictCode);

    ResponseResult<DictItemFormDetailVo> getDictItemFormDetail(Long id, String dictCode);

    ResponseResult addDictItem(String dictCode, AddDictItemDto addDictItemDto);

    ResponseResult updateDictItem(Long id, String dictCode, @Valid UpdateDictItemDto updateDictDto);

    ResponseResult deleteDictItem(Long[] ids);
}

