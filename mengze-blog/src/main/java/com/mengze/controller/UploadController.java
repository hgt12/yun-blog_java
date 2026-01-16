package com.mengze.controller;

import com.mengze.domain.ResponseResult;
import com.mengze.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

//上传头像，图片等接口
@RestController
public class UploadController
{
    @Autowired
    private UploadService uploadService;
    @PostMapping("/upload")
    public ResponseResult uploadImg(MultipartFile img)
    {
        return uploadService.uploadImg(img);
    }
}
