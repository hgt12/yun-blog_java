package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Config;
import com.mengze.domain.vo.BlogConfigVo;

import java.util.List;


/**
 * 系统配置表(Config)表服务接口
 *
 * @author makejava
 * @since 2026-05-30 20:05:24
 */
public interface ConfigService extends IService<Config> {

    ResponseResult<List<BlogConfigVo>> getBlogConfig();
}

