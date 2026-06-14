package com.mengze.service.impl;

import com.mengze.constants.OssFileConstants;
import com.mengze.constants.RedisKeyConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import com.mengze.service.UploadService;
import com.mengze.utils.PathUtils;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

//公用上传图片实现类
@Service
public class UploadServiceImpl implements UploadService
{
    @Autowired
    private RedisCache redisCache;

    @Autowired
    OssFileServiceImpl ossFileService;

    //公用的上传图片方法
    @Override
    public ResponseResult uploadImage(MultipartFile image)
    {
        //判断文件是否为空
        if (image == null || image.isEmpty()) {
            throw new SystemException(AppHttpCodeEnum.FILE_NULL);
        }
        
        //判断文件大小（限制2MB）
        long maxSize = 2 * 1024 * 1024; // 2MB
        if (image.getSize() > maxSize) {
            throw new SystemException(AppHttpCodeEnum.FILE_SIZE_ERROR);
        }
        
        //判断文件类型
        //获取原始文件名
        String originalFilename = image.getOriginalFilename();
        //判断文件名是否为png，jpg，gif，jpeg
        if (!originalFilename.endsWith(".png") && !originalFilename.endsWith(".jpg") && !originalFilename.endsWith(".gif") && !originalFilename.endsWith(".jpeg")) {
            throw new SystemException(AppHttpCodeEnum.FILE_TYPE_ERROR);
        }
        
        //上传到临时目录，文件路径格式：temp/yyyy/MM/dd/文件名.后缀
        String filePath = OssFileConstants.TEMP_OSS_DIRECTORY_PATH_PREFIX + PathUtils.generateFilePath(originalFilename);
        String url;
        try {
            url = ossFileService.uploadOss(image, filePath);
        } catch (RuntimeException ex) {
            throw new SystemException(AppHttpCodeEnum.FILE_UPLOAD_ERROR);
        }
        
        //将临时文件信息存入Redis，设置24小时过期时间
        //存储格式：key=temp:file:文件路径, value=上传时间戳
        redisCache.setCacheObject(RedisKeyConstants.TEMP_FILE_KEY_PREFIX + filePath, System.currentTimeMillis(), 24, TimeUnit.HOURS);
        
        return ResponseResult.okResult(url);
    }
}
