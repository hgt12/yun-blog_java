package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.service.SysFileService;
import com.mengze.service.UploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

//后台用户上传文件相关接口
@RestController
@RequestMapping("/files")
@Api(tags = "上传", description = "文件上传相关接口")
public class FilesController
{
    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private UploadService uploadService;

    @PostMapping("/image")
    @SystemLog(businessName = "上传图片")
    @ApiOperation(value = "上传图片", notes = "上传头像、文章图片等图片文件", response = String.class)
    public ResponseResult uploadImage(MultipartFile image)
    {
        return uploadService.uploadImage(image);
    }

    @PostMapping
    @SystemLog(businessName = "上传文件（包括图片）")
    @ApiOperation(value = "上传文件", notes = "上传包括图片等文件", response = String.class)
    public ResponseResult uploadFile(MultipartFile file)
    {
        return sysFileService.uploadFile(file);
    }
}
