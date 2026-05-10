package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.FileListDto;
import com.mengze.domain.entity.SysFile;
import com.mengze.domain.vo.FileListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysFileMapper;
import com.mengze.service.SysFileService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
@Slf4j
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService
{
    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private OssFileServiceImpl ossFileService;

    @Value("${oss.formal-path-prefix:formal/}")
    private String formalPathPrefix;

    //上传文件
    @Override
    @Transactional
    public ResponseResult uploadFile(MultipartFile file)
    {
        //判断上传的文件是否为空
        if (file == null || file.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.FILE_NULL, "文件不能为空");
        }

        //判断上传名字是否为空
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "文件名不能为空");
        }

        //上传至oss（注意是上传到正式文件里）
        String filePath = formalPathPrefix + PathUtils.generateFilePath(originalFilename);
        String url = ossFileService.uploadOss(file, filePath);
        if (!StringUtils.hasText(url)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "文件上传失败");
        }

        saveFileRecord(originalFilename, filePath, url, file.getSize(), file.getContentType(), "后台文件上传");
        return ResponseResult.okResult(url);
    }

    //获取文件列表
    @Override
    public ResponseResult<PageVo> getFileList(Integer pageNum, Integer pageSize, FileListDto fileListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<SysFile> queryWrapper = new LambdaQueryWrapper<>();

        //根据关键字(文件名字，文件来源)模糊查询文件
        if (StringUtils.hasText(fileListDto.getKeywords())){
            queryWrapper.like(SysFile::getName, fileListDto.getKeywords())
                        .like(SysFile::getFileSource, fileListDto.getKeywords());
        }

        //按创建时间倒序排序
        queryWrapper.orderByDesc(SysFile::getCreateTime);

        //分页查询
        Page<SysFile> page = new Page<>(pageNum, pageSize);
        sysFileService.page(page, queryWrapper);

        //封装成vo返回
        List<FileListVo> fileListVos = BeanCopyUtils.copyBeanList(page.getRecords(), FileListVo.class);
        PageVo pageVo = new PageVo(fileListVos, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }

    private void saveFileRecord(String fileName, String filePath, String url, Long size, String mimeType, String fileSource)
    {
        SysFile sysFile = new SysFile();
        sysFile.setName(fileName);
        sysFile.setFilePath(filePath);
        sysFile.setUrl(url);
        sysFile.setSize(size);
        sysFile.setMimeType(mimeType);
        sysFile.setFileSource(fileSource);
        sysFileService.save(sysFile);
    }
}

