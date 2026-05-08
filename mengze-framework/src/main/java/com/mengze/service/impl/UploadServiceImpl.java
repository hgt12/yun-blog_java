package com.mengze.service.impl;

import com.google.gson.Gson;
import com.mengze.domain.ResponseResult;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import com.mengze.service.UploadService;
import com.mengze.utils.PathUtils;
import com.mengze.utils.RedisCache;
import com.qiniu.common.QiniuException;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

//公用上传图片实现类
@Service
@Data
@ConfigurationProperties(prefix = "oss")
public class UploadServiceImpl implements UploadService
{
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String domain; // OSS访问域名
    
    @Autowired
    private RedisCache redisCache;
    
    // Redis key前缀，用于存储临时文件信息
    private static final String TEMP_FILE_KEY_PREFIX = "temp:file:";

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
        String filePath = "temp/" + PathUtils.generateFilePath(originalFilename);
        String url = uploadOss(image, filePath);
        
        //将临时文件信息存入Redis，设置24小时过期时间
        //存储格式：key=temp:file:文件路径, value=上传时间戳
        redisCache.setCacheObject(TEMP_FILE_KEY_PREFIX + filePath, System.currentTimeMillis(), 24, TimeUnit.HOURS);
        
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
                //返回图片的完整URL
                return domain + key;
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
