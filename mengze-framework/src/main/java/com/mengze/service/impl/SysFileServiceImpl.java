package com.mengze.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.mapper.SysFileMapper;
import com.mengze.domain.entity.SysFile;
import com.mengze.service.SysFileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 上传文件元数据表(SysFile)表服务实现类
 *
 * @author makejava
 * @since 2026-05-07 17:57:46
 */
@Service("sysFileService")
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService {

    @Override
    public ResponseResult uploadFile(MultipartFile file) {
        return null;
    }
}

