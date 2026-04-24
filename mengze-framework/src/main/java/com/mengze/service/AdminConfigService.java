package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AdminConfigListDto;
import com.mengze.domain.entity.Config;
import com.mengze.domain.vo.PageVo;

/**
 * 系统配置表(SysConfig)表服务接口
 *
 * @author makejava
 * @since 2026-04-24 13:38:11
 */
public interface AdminConfigService extends IService<Config> {

    ResponseResult<PageVo> getConfigList(Integer pageNum, Integer pageSize, AdminConfigListDto adminConfigListDto);
}

