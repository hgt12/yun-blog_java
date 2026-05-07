package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.SysFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文件元数据表(SysFile)表服务接口
 *
 * @author makejava
 * @since 2026-05-07 17:57:40
 */
public interface SysFileService extends IService<SysFile> {

    ResponseResult uploadFile(MultipartFile file);
}

