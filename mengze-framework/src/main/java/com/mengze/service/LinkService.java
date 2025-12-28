package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.Link;

/**
 * 友链(Link)表服务接口
 *
 * @author makejava
 * @since 2025-12-28 16:49:16
 */
public interface LinkService extends IService<Link> {

    ResponseResult getAllLink();
}

