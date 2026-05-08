package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.FileListDto;
import com.mengze.domain.vo.FileListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.mapper.SysFileMapper;
import com.mengze.domain.entity.SysFile;
import com.mengze.service.SysFileService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 上传文件元数据表(SysFile)表服务实现类
 *
 * @author makejava
 * @since 2026-05-07 17:57:46
 */
@Service("sysFileService")
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService
{

    @Autowired
    private SysFileService sysFileService;

    //上传文件
    @Override
    public ResponseResult uploadFile(MultipartFile file)
    {
        return null;
    }

    //获取文件列表
    @Override
    public ResponseResult<PageVo> getFileList(Integer pageNum, Integer pageSize, FileListDto fileListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<SysFile> queryWrapper = new LambdaQueryWrapper<>();

        //根据关键字模糊查询文件
        if (StringUtils.hasText(fileListDto.getKeywords())){
            queryWrapper.like(SysFile::getName, fileListDto.getKeywords());
        }

        //按创建时间升序排序
        queryWrapper.orderByAsc(SysFile::getCreateTime);

        //分页查询
        Page<SysFile> page = new Page<>(pageNum, pageSize);
        sysFileService.page(page, queryWrapper);

        //封装成vo返回
        List<FileListVo> fileListVos = BeanCopyUtils.copyBeanList(page.getRecords(), FileListVo.class);
        PageVo pageVo = new PageVo(fileListVos, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }
}

