package com.mengze.job;

import com.mengze.constants.RedisKeyConstants;
import com.mengze.domain.entity.VisitLog;
import com.mengze.service.VisitLogService;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

// 访问日志定时任务
@Component
public class VisitLogsJob
{
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private VisitLogService visitLogService;

    // 每5分钟将 visit_log 队列批量 flush 到数据库
    @Scheduled(cron = "0 0/5 * * * ?")
    public void flushVisitLogs()
    {
        List<VisitLog> logs = redisCache.popAllFromList(RedisKeyConstants.VISIT_LOG_QUEUE_KEY);
        if (logs != null && !logs.isEmpty()) {
            visitLogService.saveBatch(logs);
        }
    }
}
