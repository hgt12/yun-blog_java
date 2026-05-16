package com.mengze.utils;

import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.LongByteArray;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.Version;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

/**
 * IP地址定位工具类
 *
 * 同时支持 IPv4 / IPv6：
 * - `ip2region_v4.xdb`：IPv4 查询
 * - `ip2region_v6.xdb`：IPv6 查询
 */
@Slf4j
@Component
public class IpLocationUtils {

    private static final String IPV4_DB = "ip2region_v4.xdb";
    private static final String IPV6_DB = "ip2region_v6.xdb";
    private static volatile Searcher ipv4Searcher;
    private static volatile Searcher ipv6Searcher;

    /**
     * 初始化 ip2region 查询服务
     */
    @PostConstruct
    public void init() {
        try {
            ipv4Searcher = createSearcher(IPV4_DB, Version.IPv4);
            ipv6Searcher = createSearcher(IPV6_DB, Version.IPv6);
            log.info("IP地址定位服务初始化成功，IPv4/IPv6 均已加载");
        } catch (Exception e) {
            log.error("IP地址定位服务初始化失败: {}", e.getMessage(), e);
        }
    }

    private Searcher createSearcher(String resourcePath, Version version) throws IOException {
        ClassPathResource resource = new ClassPathResource(resourcePath);
        try (InputStream inputStream = resource.getInputStream()) {
            LongByteArray cBuff = Searcher.loadContentFromInputStream(inputStream);
            return Searcher.newWithBuffer(version, cBuff);
        }
    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy() {
        closeSearcher(ipv4Searcher);
        closeSearcher(ipv6Searcher);
        ipv4Searcher = null;
        ipv6Searcher = null;
        log.info("IP地址定位服务已关闭");
    }

    private void closeSearcher(Searcher searcher) {
        if (searcher == null) {
            return;
        }
        try {
            searcher.close();
        } catch (IOException e) {
            log.error("关闭IP地址定位服务失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据IP地址获取地域信息
     *
     * @param ip IP地址（支持IPv4和IPv6）
     * @return 地域信息，格式：国家|区域|省份|城市|ISP
     *         如果解析失败返回 null
     */
    public static String getCityByIp(String ip) {
        if (ip == null || ip.isBlank()) {
            log.warn("IP地址为空，无法解析");
            return null;
        }

        Searcher searcher = getSearcher(ip);
        if (searcher == null) {
            log.error("IP地址定位服务未初始化或不支持该IP类型: ip={}", ip);
            return null;
        }

        try {
            String region = searcher.search(ip.trim());
            log.debug("IP地址解析成功: ip={}, region={}", ip, region);
            return region;
        } catch (Exception e) {
            log.error("IP地址解析失败: ip={}, error={}", ip, e.getMessage(), e);
            return null;
        }
    }

    private static Searcher getSearcher(String ip) {
        if (isIpv6(ip)) {
            return ipv6Searcher;
        }
        return ipv4Searcher;
    }

    private static boolean isIpv6(String ip) {
        return Objects.nonNull(ip) && ip.contains(":");
    }

    /**
     * 从完整的地域信息中提取城市名称
     *
     * @param region 完整的地域信息，格式：国家|区域|省份|城市|ISP
     * @return 城市名称，如果解析失败返回 null
     */
    public static String extractCityName(String region) {
        if (region == null || region.isBlank()) {
            return null;
        }

        try {
            String[] parts = region.split("\\|");
            if (parts.length < 4) {
                return null;
            }

            String city = normalizeRegionPart(parts[3]);
            if (city != null) {
                return city;
            }

            String province = normalizeRegionPart(parts[2]);
            if (province != null) {
                return province;
            }

            return normalizeRegionPart(parts[0]);
        } catch (Exception e) {
            log.error("提取城市名称失败: region={}, error={}", region, e.getMessage(), e);
            return null;
        }
    }

    private static String normalizeRegionPart(String part) {
        if (part == null) {
            return null;
        }
        String value = part.trim();
        return value.isEmpty() || "0".equals(value) ? null : value;
    }

    /**
     * 根据IP地址直接获取城市名称
     *
     * @param ip IP地址
     * @return 城市名称，如果解析失败返回 null
     */
    public static String getCityNameByIp(String ip) {
        return extractCityName(getCityByIp(ip));
    }
}
