package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.RedisHashFieldConstants;
import com.mengze.constants.RedisKeyConstants;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.RecordVisitDto;
import com.mengze.domain.entity.*;
import com.mengze.domain.vo.SiteInfoVo;
import com.mengze.domain.vo.VisitOverviewVo;
import com.mengze.domain.vo.VisitTrendVo;
import com.mengze.mapper.SiteStatisticsMapper;
import com.mengze.mapper.UniqueVisitorMapper;
import com.mengze.mapper.VisitLogMapper;
import com.mengze.service.SiteStatisticsService;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.exception.SystemException;
import com.mengze.utils.IpLocationUtils;
import com.mengze.utils.IpUtils;
import com.mengze.utils.RedisCache;
import com.mengze.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 网站统计表(SiteStatistics)表服务实现类（前后台共用）
 */
@Service("siteStatisticsService")
@Slf4j
public class SiteStatisticsServiceImpl extends ServiceImpl<SiteStatisticsMapper, SiteStatistics> implements SiteStatisticsService
{
    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private VisitLogMapper visitLogMapper;

    @Autowired
    private UniqueVisitorMapper uniqueVisitorMapper;

    //获取网站统计信息
    @Override
    public ResponseResult getSiteInfo()
    {
        // 去数据库 site_statistics 表中取全站累计 PV/UV，如果没有数据就默认 0
        SiteStatistics siteStatistics = getById(1L); // 只查第一行 因为这个表默认就一行数据
        long totalViews = siteStatistics == null || siteStatistics.getTotalViews() == null ? 0L : siteStatistics.getTotalViews();
        long totalVisitors = siteStatistics == null || siteStatistics.getTotalVisitors() == null ? 0L : siteStatistics.getTotalVisitors();

        SiteInfoVo siteInfoVo = new SiteInfoVo(totalViews, totalVisitors);
        return ResponseResult.okResult(siteInfoVo);
    }

    //记录访问日志
    @Override
    public ResponseResult recordVisit(RecordVisitDto recordVisitDto, HttpServletRequest request)
    {
        long startTime = System.currentTimeMillis();
        String ip = IpUtils.getIpAddr(request);
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null) {
            userAgent = "";
        }

        // IP 限流：每个 IP 每分钟最多 30 次，超出直接拒绝
        String rateLimitKey = "rate:recordVisit:" + ip + ":" + (System.currentTimeMillis() / 60000);
        Long count = redisTemplate.opsForValue().increment(rateLimitKey);
        if (count == 1) {
            // 第一次写入，设置 70 秒过期
            redisTemplate.expire(rateLimitKey, 70, java.util.concurrent.TimeUnit.SECONDS);
        }
        if (count > 30) {
            throw new SystemException(AppHttpCodeEnum.IP_REQUEST_LIMIT);
        }

        // IP 脱敏：保留前三段，末段替换为 *（如 192.168.1.*）
        //String maskedIp = maskIp(ip);

        // 判断操作人：登录用户显示用户名，否则显示“游客”
        String operator = SystemConstants.OPERATOR_IS_VISITOR;
        if (SecurityUtils.isLogin()) {
            operator = Objects.requireNonNull(SecurityUtils.getLoginUser()).getUsername();
        }

        //根据ip获取地区信息
        String region = IpLocationUtils.getCityByIp(ip);
        if (region == null || region.isBlank()) {
            region = SystemConstants.UNKNOWN;
        }

        // 访问日志写入 Redis 队列，由定时任务批量保存到数据库
        VisitLog visitLog = new VisitLog();
        //visitLog.setIp(maskedIp);//使用脱敏的ip进行记录
        visitLog.setIp(ip);//不使用脱敏的ip进行记录
        visitLog.setOperator(operator);
        visitLog.setRegion(region);

        // 不存原始 UA，只保留解析后的设备信息
        if (!userAgent.isEmpty()) {
            visitLog.setDeviceType(parseDeviceType(userAgent));
            visitLog.setBrowser(parseBrowser(userAgent));
            visitLog.setOs(parseOs(userAgent));
        }

        // pageUrl 只保留路径部分，去除查询参数；为空时给默认值“/”，避免落库异常
        String pageUrl = recordVisitDto.getPageUrl();
        if (pageUrl == null || pageUrl.isBlank()) {
            pageUrl = "/";
        } else if (pageUrl.contains("?")) {
            pageUrl = pageUrl.substring(0, pageUrl.indexOf('?'));
        }
        visitLog.setPageUrl(pageUrl);
        visitLog.setPageTitle(recordVisitDto.getPageTitle());
        visitLog.setReferrer(recordVisitDto.getReferrer());
        visitLog.setMethod(request.getMethod());
        visitLog.setVisitTime(new Date());
        visitLog.setExecutionTime((int) (System.currentTimeMillis() - startTime));
        redisCache.pushToList(RedisKeyConstants.VISIT_LOG_QUEUE_KEY, visitLog);

        // PV 增量递增
        redisCache.incrementCacheMapValue(RedisKeyConstants.SITE_VIEWS_ADD_INCREMENT_KEY, RedisHashFieldConstants.SITE_TOTAL_VIEWS_FIELD, 1);

        // UV 去重：先用 Redis 做快速判断，数据库做最终兜底
        String visitorHash = DigestUtils.md5DigestAsHex((ip + userAgent).getBytes(StandardCharsets.UTF_8));
        String visitorKey = RedisKeyConstants.SITE_VISITOR_SET_KEY;
        Date now = new Date();

        // 如果是全站新访客就写入 Redis 和数据库
        boolean isNewVisitor = redisCache.addToSet(visitorKey, visitorHash);
        if (isNewVisitor) {
            UniqueVisitor uniqueVisitor = new UniqueVisitor();
            uniqueVisitor.setVisitorHash(visitorHash);
            uniqueVisitor.setIp(ip);
            uniqueVisitor.setFirstVisitTime(now);
            uniqueVisitor.setLastVisitTime(now);
            uniqueVisitor.setVisitCount(1);

            try {
                uniqueVisitorMapper.insert(uniqueVisitor);
                redisCache.incrementCacheMapValue(RedisKeyConstants.SITE_VISITORS_INCREMENT_KEY, RedisHashFieldConstants.SITE_TOTAL_VISITORS_FIELD, 1);
            } catch (org.springframework.dao.DuplicateKeyException e) {
                // 在 Redis 状态丢失或并发下数据库已存在时，转为查库更新，并补回 Redis
                redisCache.addToSet(visitorKey, visitorHash);
                UniqueVisitor existedVisitor = uniqueVisitorMapper.selectOne(new LambdaQueryWrapper<UniqueVisitor>()
                        .eq(UniqueVisitor::getVisitorHash, visitorHash)
                        .last("limit 1"));
                if (existedVisitor != null) {
                    existedVisitor.setIp(ip);
                    existedVisitor.setLastVisitTime(now);
                    existedVisitor.setVisitCount(existedVisitor.getVisitCount() == null ? 1 : existedVisitor.getVisitCount() + 1);
                    uniqueVisitorMapper.updateById(existedVisitor);
                } else {
                    throw e;
                }
            }
        } else {
            // 已存在的全站唯一访客，更新最近访问时间和访问次数
            UniqueVisitor uniqueVisitor = uniqueVisitorMapper.selectOne(new LambdaQueryWrapper<UniqueVisitor>()
                    .eq(UniqueVisitor::getVisitorHash, visitorHash)
                    .last("limit 1"));
            if (uniqueVisitor != null) {
                uniqueVisitor.setIp(ip);
                uniqueVisitor.setLastVisitTime(now);
                uniqueVisitor.setVisitCount(uniqueVisitor.getVisitCount() == null ? 1 : uniqueVisitor.getVisitCount() + 1);
                uniqueVisitorMapper.updateById(uniqueVisitor);
            }
        }

        return ResponseResult.okResult();
    }

    //获取访问趋势
    @Override
    public ResponseResult getVisitTrend(String startDate, String endDate)
    {
        //解析日期并校验 确保开始日期 ≤ 结束日期
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE);
        if (start.isAfter(end)) {
            throw new SystemException(AppHttpCodeEnum.PARAM_INVALID);
        }

        //生成日期列表
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            dates.add(date);
        }

        //将日期转换成字符串方便前端显示
        List<String> dateStrings = dates.stream()
                .map(d -> d.format(DateTimeFormatter.ISO_LOCAL_DATE))
                .collect(Collectors.toList());

        //生成每天的开始时间和结束时间
        List<Date> startTimes = dates.stream()
                .map(d -> Date.from(d.atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .toList();
        List<Date> endTimes = dates.stream()
                .map(d -> Date.from(d.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()))
                .toList();

        //初始化 PV、UV、IP
        List<Integer> pvList = new ArrayList<>(dates.size());
        List<Integer> uvList = new ArrayList<>(dates.size());
        List<Integer> ipList = new ArrayList<>(dates.size());

        //统计每天的 PV、UV、IP
        for (int i = 0; i < dates.size(); i++)
        {
            Date dayStart = startTimes.get(i);
            Date dayEnd = endTimes.get(i);

            //查询当天所有访问记录数量 PV
            long pvCount = visitLogMapper.selectCount(new LambdaQueryWrapper<VisitLog>()
                    .ge(VisitLog::getVisitTime, dayStart)
                    .lt(VisitLog::getVisitTime, dayEnd));
            pvList.add((int) pvCount);

            // 在数据库侧按 IP 分组后计数，减少数据传输和 Java 侧聚合开销
            List<VisitLog> uniqueIps = visitLogMapper.selectList(new LambdaQueryWrapper<VisitLog>()
                    .ge(VisitLog::getVisitTime, dayStart)
                    .lt(VisitLog::getVisitTime, dayEnd)
                    .select(VisitLog::getIp)
                    .groupBy(VisitLog::getIp));
            int count = uniqueIps == null ? 0 : uniqueIps.size();
            uvList.add(count);
            ipList.add(count);
        }

        //封装成vo并返回结果
        VisitTrendVo visitTrendVo = new VisitTrendVo(dateStrings, pvList, uvList, ipList);
        return ResponseResult.okResult(visitTrendVo);
    }

    //获取浏览量概览
    @Override
    public ResponseResult getVisitOverview()
    {
        // 去数据库 site_statistics 表中取全站累计 PV/UV，如果没有数据就默认 0
        SiteStatistics siteStatistics = getById(1L); // 只查第一行 因为这个表默认就一行数据
        long totalPvCount = siteStatistics == null || siteStatistics.getTotalViews() == null ? 0L : siteStatistics.getTotalViews();
        long totalUvCount = siteStatistics == null || siteStatistics.getTotalVisitors() == null ? 0L : siteStatistics.getTotalVisitors();

        //计算今天和昨天的时间范围 用于查询今天和昨天的数据
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        Date todayStart = Date.from(today.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date tomorrowStart = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date yesterdayStart = Date.from(yesterday.atStartOfDay(ZoneId.systemDefault()).toInstant());

        //查询今天和昨日天的 PV
        long todayPvCount = visitLogMapper.selectCount(new LambdaQueryWrapper<VisitLog>()
                .ge(VisitLog::getVisitTime, todayStart)
                .lt(VisitLog::getVisitTime, tomorrowStart));
        long yesterdayPvCount = visitLogMapper.selectCount(new LambdaQueryWrapper<VisitLog>()
                .ge(VisitLog::getVisitTime, yesterdayStart)
                .lt(VisitLog::getVisitTime, todayStart));

        //查询今天和昨天的 IP 列表
        List<VisitLog> todayLogs = visitLogMapper.selectList(new LambdaQueryWrapper<VisitLog>()
                .ge(VisitLog::getVisitTime, todayStart)
                .lt(VisitLog::getVisitTime, tomorrowStart)
                .select(VisitLog::getIp));
        List<VisitLog> yesterdayLogs = visitLogMapper.selectList(new LambdaQueryWrapper<VisitLog>()
                .ge(VisitLog::getVisitTime, yesterdayStart)
                .lt(VisitLog::getVisitTime, todayStart)
                .select(VisitLog::getIp));

        //查询今天和昨天去重后的 UV
        long todayUvCount = todayLogs.stream()
                .map(VisitLog::getIp)
                .filter(Objects::nonNull)
                .distinct()
                .count();
        long yesterdayUvCount = yesterdayLogs.stream()
                .map(VisitLog::getIp)
                .filter(Objects::nonNull)
                .distinct()
                .count();

        //计算增长率
        double pvGrowthRate = calcGrowthRate(todayPvCount, yesterdayPvCount);
        double uvGrowthRate = calcGrowthRate(todayUvCount, yesterdayUvCount);

        //封装成vo
        VisitOverviewVo visitOverviewVo = new VisitOverviewVo(
                Math.toIntExact(todayUvCount),
                Math.toIntExact(totalUvCount),
                uvGrowthRate,
                Math.toIntExact(todayPvCount),
                Math.toIntExact(totalPvCount),
                pvGrowthRate
        );

        return ResponseResult.okResult(visitOverviewVo);
    }


    //计算增长率方法
    private double calcGrowthRate(long current, long previous) {
        if (previous == 0) {
            return current == 0 ? 0.0 : 100.0;
        }
        return Math.round(((current - previous) * 1000.0 / previous)) / 10.0;
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
        return SystemConstants.UNKNOWN;
    }

    //解析设备类型
    private String parseOs(String ua)
    {
        if (ua.contains("Windows NT")) return "Windows";
        if (ua.contains("Mac OS X")) return "macOS";
        if (ua.contains("Android")) return "Android";
        if (ua.contains("iPhone") || ua.contains("iPad")) return "iOS";
        if (ua.contains("Linux")) return "Linux";
        return SystemConstants.UNKNOWN;
    }

    // IP 脱敏：IPv4 末段替换为 *，IPv6 后两组替换为 *
    private String maskIp(String ip)
    {
        if (ip == null || ip.isEmpty()) return SystemConstants.UNKNOWN;
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
