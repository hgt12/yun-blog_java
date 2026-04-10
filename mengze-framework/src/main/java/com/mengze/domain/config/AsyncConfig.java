package com.mengze.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置类
 * 用于配置异步邮件发送的线程池
 */
@Configuration
public class AsyncConfig
{
    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor()
    {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数：保持活跃的最小线程数
        executor.setCorePoolSize(5);
        
        // 最大线程数：允许创建的最大线程数
        executor.setMaxPoolSize(10);
        
        // 队列容量：等待队列的大小
        executor.setQueueCapacity(100);
        
        // 线程名称前缀
        executor.setThreadNamePrefix("async-email-");
        
        // 线程空闲时间：超过核心线程数的线程在空闲多久后被回收（秒）
        executor.setKeepAliveSeconds(60);
        
        // 拒绝策略：队列满时由调用线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        return executor;
    }
}
