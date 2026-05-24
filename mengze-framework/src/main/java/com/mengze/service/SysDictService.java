package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.DictListDto;
import com.mengze.domain.entity.SysDict;

/**
 * 数据字典类型表(SysDict)表服务接口
 *
 * @author makejava
 * @since 2026-05-24 13:59:24
 */
public interface SysDictService extends IService<SysDict> {

    ResponseResult getDictList(Integer pageNum, Integer pageSize, DictListDto dictListDto);

    ResponseResult getDictOptions();
}

