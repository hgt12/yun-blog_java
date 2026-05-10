package com.mengze.service;

import com.mengze.domain.vo.UploadFileMetaVo;

import java.util.List;

/**
 * OSS文件管理服务接口
 * 
 * @author mengze
 */
public interface OssFileService
{
    /**
     * 将临时文件转为正式文件
     * @param tempFileUrl 临时文件URL
     * @return 正式文件元数据
     */
    UploadFileMetaVo moveTempToFormal(String tempFileUrl);
    
    /**
     * 批量将临时文件转为正式文件
     * @param tempFileUrls 临时文件URL列表
     * @return 正式文件元数据列表
     */
    List<UploadFileMetaVo> batchMoveTempToFormal(List<String> tempFileUrls);
    
    /**
     * 删除OSS上的文件
     * @param fileKey 文件key（路径）
     * @return 是否删除成功
     */
    boolean deleteFile(String fileKey);
    
    /**
     * 根据完整URL删除OSS上的文件
     * @param fileUrl 文件完整URL
     * @return 是否删除成功
     */
    boolean deleteFileByUrl(String fileUrl);

    /**
     * 清理过期的临时文件
     * @return 清理的文件数量
     */
    int cleanExpiredTempFiles();
    
    /**
     * 将文件移动到deleted文件夹
     * @param fileUrl 文件URL
     * @return 是否移动成功
     */
    boolean moveFileToDeleted(String fileUrl);
    
    /**
     * 批量将文件移动到deleted文件夹
     * @param fileUrls 文件URL列表
     * @return 成功移动的文件数量
     */
    int batchMoveFilesToDeleted(List<String> fileUrls);
    
    /**
     * 从deleted文件夹恢复文件到正式文件夹
     * @param deletedFileUrl deleted文件夹中的文件URL
     * @return 恢复后的正式文件URL，失败返回null
     */
    String restoreFileFromDeleted(String deletedFileUrl);
    
    /**
     * 批量从deleted文件夹恢复文件到正式文件夹
     * @param deletedFileUrls deleted文件夹中的文件URL列表
     * @return 恢复后的正式文件URL列表
     */
    List<String> batchRestoreFilesFromDeleted(List<String> deletedFileUrls);
}
