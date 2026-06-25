package com.mengze.runner;

import com.mengze.constants.RedisHashFieldConstants;
import com.mengze.constants.RedisKeyConstants;
import com.mengze.domain.entity.SiteStatistics;
import com.mengze.mapper.SiteStatisticsMapper;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 应用启动时预热站点统计缓存
 * <p>
 * Redis 改为增量模式后，不再将数据库中的历史总量写入 Redis。
 * Redis 中 PV/UV 的值仅代表「自上次同步以来的增量」，
 * 启动时从 0 开始，HINCRBY 首次调用会自动创建 key。
 * <p>
 * 本 Runner 仅负责：
 * <ol>
 *   <li>确保数据库 site_statistics 表中 id=1 的行存在（不存在则创建）</li>
 *   <li>确保 Redis 中的增量 hash 结构已初始化（设为 0，避免 getCacheMapValue 返回 null）</li>
 * </ol>
 *
 * @author makejava
 * @since 2026-06-25
 */
@Component
@Order(2)
public class SiteStatisticsCacheRunner implements CommandLineRunner
{
    @Autowired
    private SiteStatisticsMapper siteStatisticsMapper;

    @Autowired
    private RedisCache redisCache;

    @Override
    public void run(String... args)
    {
        // 确保数据库中存在 site_statistics 记录（id=1），不存在则初始化
        SiteStatistics statistics = siteStatisticsMapper.selectById(1L);
        if (statistics == null) {
            statistics = new SiteStatistics();
            statistics.setId(1L);
            statistics.setTotalViews(0L);
            statistics.setTotalVisitors(0L);
            siteStatisticsMapper.insert(statistics);
            System.out.println("! ========> 站点统计记录已初始化（DB）<======== !");
        }

        // 初始化 Redis 增量计数器为 0（HINCRBY 首次调用时也会自动创建，这里显式初始化避免返回 null）
        redisCache.setCacheMapValue(RedisKeyConstants.SITE_TOTAL_VIEWS_KEY,
                RedisHashFieldConstants.SITE_TOTAL_VIEWS_FIELD, 0);
        redisCache.setCacheMapValue(RedisKeyConstants.SITE_TOTAL_VISITORS_KEY,
                RedisHashFieldConstants.SITE_TOTAL_VISITORS_FIELD, 0);

        System.out.println("! ========> 站点 PV / UV 增量计数器已就绪（增量模式）<======== !");
    }
}
