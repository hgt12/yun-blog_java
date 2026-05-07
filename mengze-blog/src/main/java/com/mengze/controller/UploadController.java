package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.service.UploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

//前台上传头像
@RestController
@Api(tags = "上传", description = "文件上传相关接口")
public class UploadController
{
    @Autowired
    private UploadService uploadService;

    @PostMapping("/uploadAvatar")
    @SystemLog(businessName = "上传图片")
    @ApiOperation(value = "上传图片", notes = "上传头像", response = String.class)
    public ResponseResult uploadAvatar(MultipartFile avatar)
    {
        return uploadService.uploadAvatar(avatar);
    }
}
