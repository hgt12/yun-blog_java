package com.mengze.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.FileListDto;
import com.mengze.domain.entity.SysFile;
import com.mengze.domain.vo.PageVo;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

/**
 * 上传文件元数据表(SysFile)表服务接口
 *
 * @author makejava
 * @since 2026-05-07 17:57:40
 */
public interface SysFileService extends IService<SysFile> {

    ResponseResult uploadFile(MultipartFile file);

    ResponseResult<PageVo> getFileList(Integer pageNum, Integer pageSize, @Valid FileListDto fileListDto);

    ResponseResult deleteFile(Long[] ids);
}

