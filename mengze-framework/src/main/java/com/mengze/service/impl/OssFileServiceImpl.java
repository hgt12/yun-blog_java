package com.mengze.service.impl;

import com.mengze.service.OssFileService;
import com.mengze.utils.ImageUrlUtils;
import com.mengze.utils.RedisCache;
import com.qiniu.common.QiniuException;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.util.Auth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OSS文件管理服务实现类
 * 
 * @author mengze
 */
@Service
@Slf4j
public class OssFileServiceImpl implements OssFileService
{
    @Autowired
    private OssUploadServiceImpl ossUploadService;
    
    @Autowired
    private RedisCache redisCache;
    
    private static final String TEMP_FILE_KEY_PREFIX = "temp:file:";
    private static final String TEMP_PATH_PREFIX = "temp/";
    private static final String DELETED_PATH_PREFIX = "deleted/";
    
    /**
     * 获取BucketManager实例
     */
    private BucketManager getBucketManager()
    {
        Configuration cfg = new Configuration(Region.autoRegion());
        Auth auth = Auth.create(ossUploadService.getAccessKey(), ossUploadService.getSecretKey());
        return new BucketManager(auth, cfg);
    }

    // 临时 > 正式
    @Override
    public String moveTempToFormal(String tempFileUrl)
    {
        if (!StringUtils.hasText(tempFileUrl) || !tempFileUrl.contains(TEMP_PATH_PREFIX)) {
            return tempFileUrl; // 不是临时文件，直接返回
        }
        
        try {
            // 从URL中提取文件key
            String tempKey = extractKeyFromUrl(tempFileUrl);
            
            // 生成正式文件的key（去掉temp/前缀）
            String formalKey = tempKey.replace(TEMP_PATH_PREFIX, "");
            
            // 在OSS上移动文件（实际是复制+删除）
            BucketManager bucketManager = getBucketManager();
            bucketManager.copy(ossUploadService.getBucket(), tempKey, ossUploadService.getBucket(), formalKey, true);
            
            // 删除临时文件
            bucketManager.delete(ossUploadService.getBucket(), tempKey);
            
            // 从Redis中删除临时文件记录
            redisCache.deleteObject(TEMP_FILE_KEY_PREFIX + tempKey);
            
            // 返回正式文件URL
            String formalUrl = ossUploadService.getDomain() + formalKey;
            log.info("文件转正成功: {} -> {}", tempFileUrl, formalUrl);
            return formalUrl;
            
        } catch (QiniuException e) {
            log.error("文件转正失败: {}", tempFileUrl, e);
            return tempFileUrl; // 失败时返回原URL
        }
    }

    //批量 临时 > 正式
    @Override
    public List<String> batchMoveTempToFormal(List<String> tempFileUrls)
    {
        if (tempFileUrls == null || tempFileUrls.isEmpty()) {
            return new ArrayList<>();
        }
        
        return tempFileUrls.stream()
                .map(this::moveTempToFormal)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean deleteFile(String fileKey)
    {
        try {
            BucketManager bucketManager = getBucketManager();
            bucketManager.delete(ossUploadService.getBucket(), fileKey);
            log.info("从oss中删除文件成功: {}", fileKey);
            return true;
        } catch (QiniuException e) {
            log.error("从oss中删除文件失败: {}", fileKey, e);
            return false;
        }
    }
    
    @Override
    public boolean deleteFileByUrl(String fileUrl)
    {
        if (!StringUtils.hasText(fileUrl)) {
            return false;
        }
        
        // 从URL中提取文件key
        String fileKey = extractKeyFromUrl(fileUrl);
        
        if (!StringUtils.hasText(fileKey)) {
            log.warn("无法从URL提取文件key: {}", fileUrl);
            return false;
        }
        
        return deleteFile(fileKey);
    }
    
    @Override
    public String processContentImages(String content)
    {
        if (!ImageUrlUtils.containsTempImages(content)) {
            return content;
        }
        
        // 提取所有临时图片URL
        List<String> tempImageUrls = ImageUrlUtils.extractTempImageUrls(content);
        
        // 逐个转换为正式URL
        String result = content;
        for (String tempUrl : tempImageUrls) {
            String formalUrl = moveTempToFormal(tempUrl);
            result = ImageUrlUtils.replaceImageUrl(result, tempUrl, formalUrl);
        }
        
        return result;
    }
    
    @Override
    public int cleanExpiredTempFiles()
    {
        int cleanedCount = 0;
        
        try {
            // 从Redis获取所有临时文件记录
            Collection<String> keys = redisCache.keys(TEMP_FILE_KEY_PREFIX + "*");
            
            if (keys == null || keys.isEmpty()) {
                log.info("没有需要清理的临时文件");
                return 0;
            }
            
            long currentTime = System.currentTimeMillis();
            //long expireTime = 60 * 1000; // 测试1分钟触发一次
            long expireTime = 24 * 60 * 60 * 1000; // 24小时
            
            for (String redisKey : keys) {
                Object uploadTimeObj = redisCache.getCacheObject(redisKey);
                
                // 类型安全转换
                Long uploadTime = null;
                if (uploadTimeObj instanceof Long) {
                    uploadTime = (Long) uploadTimeObj;
                } else if (uploadTimeObj instanceof String) {
                    try {
                        uploadTime = Long.parseLong((String) uploadTimeObj);
                    } catch (NumberFormatException e) {
                        log.warn("无法解析上传时间: {}, 值: {}", redisKey, uploadTimeObj);
                        continue;
                    }
                } else if (uploadTimeObj instanceof Number) {
                    uploadTime = ((Number) uploadTimeObj).longValue();
                }
                
                // 如果文件已过期（超过24小时）
                if (uploadTime != null && (currentTime - uploadTime) > expireTime) {
                    // 提取文件key
                    String fileKey = redisKey.replace(TEMP_FILE_KEY_PREFIX, "");
                    
                    // 删除OSS上的文件
                    if (deleteFile(fileKey)) {
                        // 删除Redis记录
                        redisCache.deleteObject(redisKey);
                        cleanedCount++;
                        log.info("清理过期临时文件: {}", fileKey);
                    }
                }
            }
            
            log.info("临时文件清理完成，共清理 {} 个文件", cleanedCount);
            
        } catch (Exception e) {
            log.error("清理临时文件时发生错误", e);
        }
        
        return cleanedCount;
    }
    
    /**
     * 从URL中提取文件key
     */
    private String extractKeyFromUrl(String url)
    {
        if (!StringUtils.hasText(url)) {
            return "";
        }
        
        // 如果URL包含域名，去掉域名部分
        if (url.contains("://")) {
            int index = url.indexOf("/", url.indexOf("://") + 3);
            if (index > 0) {
                return url.substring(index + 1);
            }
        }
        
        return url;
    }

    // 正式 > 已删除
    @Override
    public boolean moveFileToDeleted(String fileUrl)
    {
        if (!StringUtils.hasText(fileUrl)) {
            log.warn("文件URL为空，无法移动到deleted文件夹");
            return false;
        }
        
        try {
            // 从URL中提取文件key
            String sourceKey = extractKeyFromUrl(fileUrl);
            
            if (!StringUtils.hasText(sourceKey)) {
                log.warn("无法从URL提取文件key: {}", fileUrl);
                return false;
            }
            
            // 如果文件已经在deleted文件夹中，直接返回成功
            if (sourceKey.startsWith(DELETED_PATH_PREFIX)) {
                log.info("文件已在deleted文件夹中: {}", sourceKey);
                return true;
            }
            
            // 生成 deleted key
            String targetKey = DELETED_PATH_PREFIX + sourceKey;
            
            // 在OSS上移动文件（实际是复制+删除）
            BucketManager bucketManager = getBucketManager();
            bucketManager.copy(ossUploadService.getBucket(), sourceKey, ossUploadService.getBucket(), targetKey, true);
            
            // 删除源文件
            bucketManager.delete(ossUploadService.getBucket(), sourceKey);
            
            log.info("文件移动到deleted文件夹成功: {} -> {}", sourceKey, targetKey);
            return true;
            
        } catch (QiniuException e) {
            log.error("文件移动到deleted文件夹失败: {}", fileUrl, e);
            return false;
        }
    }

    //批量 正式 > 已删除
    @Override
    public int batchMoveFilesToDeleted(List<String> fileUrls)
    {
        if (fileUrls == null || fileUrls.isEmpty()) {
            return 0;
        }
        
        int successCount = 0;
        for (String fileUrl : fileUrls) {
            if (moveFileToDeleted(fileUrl)) {
                successCount++;
            }
        }
        
        log.info("批量移动文件到deleted文件夹完成，成功: {}/{}", successCount, fileUrls.size());
        return successCount;
    }

    // 已删除 > 正式 （恢复操作）
    @Override
    public String restoreFileFromDeleted(String deletedFileUrl)
    {
        if (!StringUtils.hasText(deletedFileUrl)) {
            log.warn("文件URL为空，无法从deleted文件夹恢复");
            return null;
        }
        
        try {
            // 从URL中提取文件key
            String deletedKey = extractKeyFromUrl(deletedFileUrl);
            
            if (!StringUtils.hasText(deletedKey)) {
                log.warn("无法从URL提取文件key: {}", deletedFileUrl);
                return null;
            }
            
            // 检查文件是否在deleted文件夹中
            if (!deletedKey.startsWith(DELETED_PATH_PREFIX)) {
                log.warn("文件不在deleted文件夹中，无需恢复: {}", deletedKey);
                return deletedFileUrl; // 返回原URL
            }
            
            // 生成恢复后的文件key（去掉deleted/前缀）
            String restoredKey = deletedKey.substring(DELETED_PATH_PREFIX.length());
            
            // 在OSS上移动文件（实际是复制+删除）
            BucketManager bucketManager = getBucketManager();
            bucketManager.copy(ossUploadService.getBucket(), deletedKey, ossUploadService.getBucket(), restoredKey, true);
            
            // 删除deleted文件夹中的文件
            bucketManager.delete(ossUploadService.getBucket(), deletedKey);
            
            // 返回恢复后的文件URL
            String restoredUrl = ossUploadService.getDomain() + restoredKey;
            log.info("文件从deleted文件夹恢复成功: {} -> {}", deletedKey, restoredKey);
            return restoredUrl;
            
        } catch (QiniuException e) {
            log.error("文件从deleted文件夹恢复失败: {}", deletedFileUrl, e);
            return null;
        }
    }

    //批量 已删除 > 正式 （恢复操作）
    @Override
    public List<String> batchRestoreFilesFromDeleted(List<String> deletedFileUrls)
    {
        if (deletedFileUrls == null || deletedFileUrls.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<String> restoredUrls = new ArrayList<>();
        int successCount = 0;
        
        for (String deletedFileUrl : deletedFileUrls) {
            String restoredUrl = restoreFileFromDeleted(deletedFileUrl);
            if (restoredUrl != null) {
                restoredUrls.add(restoredUrl);
                successCount++;
            }
        }
        
        log.info("批量从deleted文件夹恢复文件完成，成功: {}/{}", successCount, deletedFileUrls.size());
        return restoredUrls;
    }
}
