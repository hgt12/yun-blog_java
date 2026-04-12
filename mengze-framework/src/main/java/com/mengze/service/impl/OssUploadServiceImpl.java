package com.mengze.service.impl;

import com.google.gson.Gson;
import com.mengze.domain.ResponseResult;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import com.mengze.service.UploadService;
import com.mengze.utils.PathUtils;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

//Oss上传图片实现类
@Service
@Data
@ConfigurationProperties(prefix = "oss")
public class OssUploadServiceImpl implements UploadService
{
    private String accessKey;
    private String secretKey;
    private String bucket;

    @Override
    public ResponseResult uploadImg(MultipartFile img)
    {
        //判断文件是否为空
        if (img == null || img.isEmpty()) {
            throw new SystemException(AppHttpCodeEnum.FILE_TYPE_ERROR);
        }
        
        //判断文件大小（限制2MB）
        long maxSize = 2 * 1024 * 1024; // 2MB
        if (img.getSize() > maxSize) {
            throw new SystemException(AppHttpCodeEnum.FILE_SIZE_ERROR);
        }
        
        //判断文件类型
        //获取原始文件名
        String originalFilename = img.getOriginalFilename();
        //判断文件名是否为png，jpg，gif，jpeg
        if (!originalFilename.endsWith(".png") && !originalFilename.endsWith(".jpg") && !originalFilename.endsWith(".gif") && !originalFilename.endsWith(".jpeg")) {
            throw new SystemException(AppHttpCodeEnum.FILE_TYPE_ERROR);
        }
        //如果判断通过就上传到Oss
        String filePath = PathUtils.generateFilePath(originalFilename);
        String url = uploadOss(img, filePath); //文件储存方式为：yyyy/MM/dd/文件名.后缀
        return ResponseResult.okResult(url);
    }


    private String uploadOss(MultipartFile imgFile, String filePath)
    {
        //构造一个带指定 Region 对象的配置类
        Configuration cfg = new Configuration(Region.autoRegion());
        cfg.resumableUploadAPIVersion = Configuration.ResumableUploadAPIVersion.V2;// 指定分片上传版本
        //...其他参数参考类注释
        UploadManager uploadManager = new UploadManager(cfg);
        //默认不指定key的情况下，以文件内容的hash值作为文件名
        String key = filePath;
        try {
            InputStream inputStream = imgFile.getInputStream();
            Auth auth = Auth.create(accessKey, secretKey);
            String upToken = auth.uploadToken(bucket);
            try {
                Response response =
                        uploadManager.put(inputStream,key,upToken,null, null);
                //解析上传成功的结果
                DefaultPutRet putRet = new
                        Gson().fromJson(response.bodyString(), DefaultPutRet.class);
                System.out.println(putRet.key);
                System.out.println(putRet.hash);
                //图片的外链不建议写死，现在为了测试方便先写死，到时候换成别的，注意这个外链链接试用30天
                return "http://t8ul2nlc8.hn-bkt.clouddn.com/" + key;
            } catch (QiniuException ex) {
                Response r = ex.response;
                System.err.println(r.toString());
                try {
                    System.err.println(r.bodyString());
                } catch (QiniuException ex2) {
                    //ignore
                }
            }
        } catch (Exception ex) {
            //ignore
        }
        return key;
    }
}
