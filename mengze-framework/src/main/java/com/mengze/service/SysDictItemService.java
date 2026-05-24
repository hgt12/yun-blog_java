package com.mengze.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.DictItemListDto;
import com.mengze.domain.entity.SysDict;
import com.mengze.domain.entity.SysDictItem;

/**
 * 数据字典项表(SysDictItem)表服务接口
 *
 * @author makejava
 * @since 2026-05-24 13:59:52
 */
public interface SysDictItemService extends IService<SysDictItem> {

    ResponseResult getDictItemList(Integer pageNum, Integer pageSize, String dictCode, DictItemListDto dictItemListDto);

}

