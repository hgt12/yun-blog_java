package com.mengze.job;

import com.mengze.service.OssFileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 清理临时文件定时任务
 * 每天凌晨2点执行，清理超过24小时的临时文件
 * 
 * @author mengze
 */
@Component
@Slf4j
public class CleanTempFilesJob
{
    @Autowired
    private OssFileService ossFileService;
    
    /**
     * 每天凌晨2点执行清理任务
     * cron表达式：秒 分 时 日 月 周
     */
    //@Scheduled(cron = "0 * * * * ?") //测试1分钟执行一次
    @Scheduled(cron = "0 0 2 * * ?") //凌晨两点钟执行
    public void cleanExpiredTempFiles()
    {
        log.info("开始执行临时文件清理任务");
        
        try {
            int cleanedCount = ossFileService.cleanExpiredTempFiles();
            log.info("临时文件清理任务完成，共清理 {} 个文件", cleanedCount);
        } catch (Exception e) {
            log.error("临时文件清理任务执行失败", e);
        }
    }
}
