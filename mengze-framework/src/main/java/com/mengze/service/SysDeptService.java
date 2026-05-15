package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.SysDept;
import com.mengze.domain.vo.DeptOptionVo;

import java.util.List;

/**
 * 部门表(SysDept)表服务接口
 *
 * @author makejava
 * @since 2026-05-15 13:06:17
 */
public interface SysDeptService extends IService<SysDept> {

    ResponseResult<List<DeptOptionVo>> getDeptOptions();
}

