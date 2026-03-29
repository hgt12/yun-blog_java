package com.mengze.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 城市位置ID查询工具类
 * 根据城市名称查询和风天气的Location_ID
 */
@Slf4j
@Component
public class CityLocationUtils {

    private static Map<String, String> cityLocationMap = new HashMap<>();

    /**
     * 初始化城市位置映射表
     */
    @PostConstruct
    public void init() {
        log.info("开始初始化城市位置映射表...");
        try {
            ClassPathResource resource = new ClassPathResource("china_city_list.json");
            
            if (!resource.exists()) {
                log.error("找不到 china_city_list.json 文件，请确保文件在 classpath 中");
                return;
            }
            
            log.info("找到 china_city_list.json 文件，开始加载...");
            
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                String jsonContent = reader.lines().collect(Collectors.joining());
                JSONArray cityArray = JSON.parseArray(jsonContent);
                
                log.info("JSON解析成功，城市数组大小: {}", cityArray.size());
                
                for (int i = 0; i < cityArray.size(); i++) {
                    JSONObject city = cityArray.getJSONObject(i);
                    String locationNameZh = city.getString("Location_Name_ZH");
                    String locationId = city.getString("Location_ID");
                    
                    if (StringUtils.hasText(locationNameZh) && StringUtils.hasText(locationId)) {
                        cityLocationMap.put(locationNameZh, locationId);
                    }
                }
                
                log.info("城市位置映射表初始化成功，共加载 {} 个国内城市", cityLocationMap.size());
                
                // 输出几个示例验证
                log.info("示例城市映射 - 北京: {}", cityLocationMap.get("北京"));
                log.info("示例城市映射 - 上海: {}", cityLocationMap.get("上海"));
            }
        } catch (Exception e) {
            log.error("城市位置映射表初始化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据城市名称获取Location_ID
     * 
     * @param cityName 城市名称（中文）
     * @return Location_ID，如果未找到返回原城市名称
     */
    public static String getLocationIdByCityName(String cityName) {
        if (!StringUtils.hasText(cityName)) {
            return cityName;
        }

        // 直接匹配
        String locationId = cityLocationMap.get(cityName);
        if (locationId != null) {
            log.info("城市名称匹配成功: cityName={}, locationId={}", cityName, locationId);
            return locationId;
        }

        // 模糊匹配：去掉"市"、"省"等后缀再尝试
        String simplifiedName = cityName.replaceAll("[市省自治区特别行政区]", "");
        locationId = cityLocationMap.get(simplifiedName);
        if (locationId != null) {
            log.info("城市名称模糊匹配成功: cityName={}, simplifiedName={}, locationId={}", 
                    cityName, simplifiedName, locationId);
            return locationId;
        }

        // 尝试包含匹配
        for (Map.Entry<String, String> entry : cityLocationMap.entrySet()) {
            if (entry.getKey().contains(simplifiedName) || simplifiedName.contains(entry.getKey())) {
                log.info("城市名称包含匹配成功: cityName={}, matchedCity={}, locationId={}", 
                        cityName, entry.getKey(), entry.getValue());
                return entry.getValue();
            }
        }

        log.warn("未找到城市对应的Location_ID，使用原城市名称: cityName={}", cityName);
        return cityName;  // 如果找不到，返回原城市名称
    }
}
