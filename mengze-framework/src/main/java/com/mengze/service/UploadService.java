package com.mengze.service;

import com.mengze.domain.ResponseResult;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    ResponseResult uploadImage(MultipartFile image);

}
