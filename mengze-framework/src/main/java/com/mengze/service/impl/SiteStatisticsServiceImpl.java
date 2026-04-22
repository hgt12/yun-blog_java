package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.RecordVisitDto;
import com.mengze.domain.entity.*;
import com.mengze.domain.vo.SiteInfoVo;
import com.mengze.mapper.CategoryMapper;
import com.mengze.mapper.SiteStatisticsMapper;
import com.mengze.mapper.TagMapper;
import com.mengze.service.ArticleService;
import com.mengze.service.SiteStatisticsService;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import com.mengze.utils.IpUtils;
import com.mengze.utils.RedisCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 网站统计表(SiteStatistics)表服务实现类
 */
@Service("siteStatisticsService")
public class SiteStatisticsServiceImpl extends ServiceImpl<SiteStatisticsMapper, SiteStatistics> implements SiteStatisticsService {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisTemplate redisTemplate;

    //获取网站统计信息
    @Override
    public ResponseResult getSiteInfo()
    {
        //从Redis获取总预览量（PV）
        Integer cachedViews = redisCache.getCacheMapValue(SystemConstants.SITE_TOTAL_VIEWS, SystemConstants.SITE_TOTAL_VIEWS_FIELD);
        long totalViews = cachedViews != null ? cachedViews.longValue() : 0L;

        //从Redis获取总访客数（UV）
        Integer cachedVisitors = redisCache.getCacheMapValue(SystemConstants.SITE_TOTAL_VISITORS, SystemConstants.SITE_TOTAL_VISITORS_FIELD);
        long totalVisitors = cachedVisitors != null ? cachedVisitors.longValue() : 0L;

        //查询文章总数
        int articleCount = (int) articleService.count(
                new LambdaQueryWrapper<Article>()
                        .eq(Article::getStatus, SystemConstants.STATUS_NORMAL)
        );

        //查询分类总数
        int categoryCount = categoryMapper.selectCount(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, SystemConstants.STATUS_NORMAL)
        ).intValue();

        //查询标签总数
        int tagCount = tagMapper.selectCount(
                new LambdaQueryWrapper<Tag>()
        ).intValue();

        SiteInfoVo siteInfoVo = new SiteInfoVo(totalViews, totalVisitors, articleCount, categoryCount, tagCount);
        return ResponseResult.okResult(siteInfoVo);
    }

    //记录访问日志
    @Override
    public ResponseResult recordVisit(RecordVisitDto recordVisitDto, HttpServletRequest request)
    {
        String ip = IpUtils.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "";
        }

        // IP 限流：每个 IP 每分钟最多 30 次，超出直接拒绝
        String rateLimitKey = "rate:recordVisit:" + ip + ":" + (System.currentTimeMillis() / 60000);
        Long count = redisTemplate.opsForValue().increment(rateLimitKey);
        if (count == 1) {
            // 第一次写入，设置 70 秒过期（略大于 1 分钟，防止边界问题）
            redisTemplate.expire(rateLimitKey, 70, java.util.concurrent.TimeUnit.SECONDS);
        }
        if (count > 30) {
            throw new SystemException(AppHttpCodeEnum.IP_REQUEST_LIMIT);
        }

        // IP 脱敏：保留前三段，末段替换为 *（如 192.168.1.*）
        String maskedIp = maskIp(ip);

        // 访问日志写入 Redis 队列，由定时任务批量 flush 到数据库
        VisitLog visitLog = new VisitLog();
        visitLog.setIp(maskedIp);
        // 不存原始 UA，只保留解析后的设备信息
        if (!userAgent.isEmpty()) {
            visitLog.setDeviceType(parseDeviceType(userAgent));
            visitLog.setBrowser(parseBrowser(userAgent));
            visitLog.setOs(parseOs(userAgent));
        }
        // pageUrl 只保留路径部分，去除查询参数
        String pageUrl = recordVisitDto.getPageUrl();
        if (pageUrl != null && pageUrl.contains("?")) {
            pageUrl = pageUrl.substring(0, pageUrl.indexOf('?'));
        }
        visitLog.setPageUrl(pageUrl);
        visitLog.setPageTitle(recordVisitDto.getPageTitle());
        visitLog.setReferrer(recordVisitDto.getReferrer());
        visitLog.setVisitTime(new Date());
        redisCache.pushToList(SystemConstants.VISIT_LOG_QUEUE, visitLog);

        // PV 计数递增
        redisCache.incrementCacheMapValue(SystemConstants.SITE_TOTAL_VIEWS, SystemConstants.SITE_TOTAL_VIEWS_FIELD, 1);

        // UV 去重：按月分 key，SADD 返回 true 说明是本月新访客
        String visitorHash = DigestUtils.md5DigestAsHex((ip + userAgent).getBytes(StandardCharsets.UTF_8));
        String monthKey = SystemConstants.SITE_VISITOR_SET_PREFIX
                + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        boolean isNewVisitor = redisCache.addToSet(monthKey, visitorHash);
        if (isNewVisitor) {
            redisCache.incrementCacheMapValue(SystemConstants.SITE_TOTAL_VISITORS, SystemConstants.SITE_TOTAL_VISITORS_FIELD, 1);
        }

        return ResponseResult.okResult();
    }

    //解析设备系统类型
    private String parseDeviceType(String ua)
    {
        if (ua.contains("Mobile") || ua.contains("Android") && !ua.contains("Tablet")) {
            return "Mobile";
        } else if (ua.contains("Tablet") || ua.contains("iPad")) {
            return "Tablet";
        }
        return "PC";
    }

    //解析浏览器类型
    private String parseBrowser(String ua)
    {
        if (ua.contains("Edg/") || ua.contains("Edge/")) return "Edge";
        if (ua.contains("OPR/") || ua.contains("Opera")) return "Opera";
        if (ua.contains("Chrome")) return "Chrome";
        if (ua.contains("Firefox")) return "Firefox";
        if (ua.contains("Safari") && !ua.contains("Chrome")) return "Safari";
        if (ua.contains("MSIE") || ua.contains("Trident")) return "IE";
        return "Unknown";
    }

    //解析设备类型
    private String parseOs(String ua)
    {
        if (ua.contains("Windows NT")) return "Windows";
        if (ua.contains("Mac OS X")) return "macOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        if (ua.contains("Linux")) return "Linux";
        return "Unknown";
    }

    // IP 脱敏：IPv4 末段替换为 *，IPv6 后两组替换为 *
    private String maskIp(String ip)
    {
        if (ip == null || ip.isEmpty()) return "unknown";
        if (ip.contains(":")) {
            // IPv6：取前6组，后两组用 * 代替
            String[] parts = ip.split(":");
            if (parts.length >= 2) {
                parts[parts.length - 1] = "*";
                parts[parts.length - 2] = "*";
                return String.join(":", parts);
            }
            return ip;
        }
        // IPv4：末段替换为 *
        int lastDot = ip.lastIndexOf('.');
        if (lastDot > 0) {
            return ip.substring(0, lastDot) + ".*";
        }
        return ip;
    }
}
