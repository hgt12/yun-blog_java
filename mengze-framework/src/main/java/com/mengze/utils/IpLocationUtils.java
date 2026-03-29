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

/**
 * IP地址定位工具类
 * 使用 ip2region 离线库解析IP地址获取地理位置信息
 */
@Slf4j
@Component
public class IpLocationUtils
{

    private static Searcher searcher;

    /**
     * 初始化 ip2region 查询服务
     */
    @PostConstruct
    public void init()
    {
        System.out.println(Searcher.class.getProtectionDomain().getCodeSource().getLocation());
        try {
            // 从 classpath 加载 xdb 文件
            ClassPathResource resource = new ClassPathResource("ip2region_v4.xdb");
            
            // 使用 LongByteArray 加载内容
            LongByteArray cBuff = Searcher.loadContentFromInputStream(resource.getInputStream());

            // 创建 Searcher 对象（默认使用 IPv4）
            searcher = Searcher.newWithBuffer(Version.IPv4, cBuff);
            
            log.info("IP地址定位服务初始化成功");
        } catch (Exception e) {
            log.error("IP地址定位服务初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 销毁资源
     */
    @PreDestroy
    public void destroy()
    {
        if (searcher != null) {
            try {
                searcher.close();
                log.info("IP地址定位服务已关闭");
            } catch (IOException e) {
                log.error("关闭IP地址定位服务失败: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * 根据IP地址获取地域信息
     * 
     * @param ip IP地址（支持IPv4和IPv6）
     * @return 地域信息，格式：国家|区域|省份|城市|ISP
     *         如果解析失败返回 null
     */
    public static String getCityByIp(String ip)
    {
        if (ip == null || ip.isEmpty()) {
            log.warn("IP地址为空，无法解析");
            return null;
        }

        if (searcher == null) {
            log.error("IP地址定位服务未初始化");
            return null;
        }

        try {
            String region = searcher.search(ip);
            log.info("IP地址解析成功: ip={}, region={}", ip, region);
            return region;
        } catch (Exception e) {
            log.error("IP地址解析失败: ip={}, error={}", ip, e.getMessage(), e);
            return null;
        }
    }

    /**
     * 从完整的地域信息中提取城市名称
     * 
     * @param region 完整的地域信息，格式：国家|区域|省份|城市|ISP
     * @return 城市名称，如果解析失败返回 null
     */
    public static String extractCityName(String region)
    {
        if (region == null || region.isEmpty()) {
            return null;
        }

        try {
            String[] parts = region.split("\\|");
            if (parts.length < 4) {
                return null;
            }
            
            String city = parts[3];  // 城市在第4个位置（索引3）
            if (!"0".equals(city) && !city.isEmpty()) {
                return city;
            }
            
            String province = parts[2];  // 省份在第3个位置（索引2）
            if (!"0".equals(province) && !province.isEmpty()) {
                return province;
            }
            
            // 如果省份也为空，使用国家
            return parts[0];
        } catch (Exception e) {
            log.error("提取城市名称失败: region={}, error={}", region, e.getMessage());
            return null;
        }
    }

    /**
     * 根据IP地址直接获取城市名称（简化版）
     * 
     * @param ip IP地址
     * @return 城市名称，如果解析失败返回 null
     */
    public static String getCityNameByIp(String ip) {
        String region = getCityByIp(ip);
        return extractCityName(region);
    }
}
