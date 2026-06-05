package com.mengze.service.impl;

import com.mengze.constants.RedisKeyConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.QWeatherResponseVo;
import com.mengze.domain.vo.WeatherNowVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.service.WeatherService;
import com.mengze.utils.CityLocationUtils;
import com.mengze.utils.IpLocationUtils;
import com.mengze.utils.IpUtils;
import com.mengze.utils.QWeatherJwtUtil;
import com.mengze.utils.RedisCache;
import com.mengze.utils.WeatherTypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * 天气接口服务类
 */
@Service("weatherService")
@Slf4j
public class WeatherServiceImpl implements WeatherService
{

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${weather.api.key-id}")
    private String keyId;

    @Value("${weather.api.project-id}")
    private String projectId;

    @Value("${weather.api.host}")
    private String weatherApiHost;

    @Value("${weather.api.cache-minutes:30}")
    private Integer cacheMinutes;

    @Value("${weather.api.private-key}")
    private String privateKey;

    @Override
    public ResponseResult getWeatherNow(String location, String lang, String unit)
    {
        // 解析并获取城市位置
        location = resolveLocation(location);
        
        // 设置默认参数
        lang = StringUtils.hasText(lang) ? lang : "zh";
        unit = StringUtils.hasText(unit) ? unit : "m";

        // 构建缓存key并尝试从缓存获取
        String cacheKey = RedisKeyConstants.WEATHER_NOW_KEY_PREFIX + location + ":" + lang + ":" + unit;
        WeatherNowVo cachedWeather = redisCache.getCacheObject(cacheKey);
        if (cachedWeather != null) {
            log.info("从缓存获取天气数据: location={}", location);
            return ResponseResult.okResult(cachedWeather);
        }

        try {
            // 调用和风天气API获取数据
            ResponseEntity<QWeatherResponseVo<WeatherNowVo>> response = callWeatherApi(location, lang, unit);
            
            // 处理API响应
            return handleWeatherResponse(response, cacheKey, location);
        } catch (Exception e) {
            log.error("调用和风天气API异常: location={}, error={}", location, e.getMessage(), e);
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(),
                    "获取天气信息异常：" + e.getMessage());
        }
    }

    /**
     * 解析并获取城市位置
     * 如果location为空，则根据客户端IP自动解析城市
     * 无论如何都会将城市名称转换为Location_ID
     * 
     * @param location 城市位置（可以是城市名称或Location_ID）
     * @return 解析后的城市位置ID
     */
    private String resolveLocation(String location)
    {
        String cityName;
        
        // 如果传入了location参数，直接使用
        if (StringUtils.hasText(location)) {
            cityName = location;
            log.info("使用传入的城市参数: {}", cityName);
        } else {
            // 否则根据IP解析城市
            cityName = resolveCityNameByIp();
        }
        
        // 将城市名称转换为Location_ID
        String locationId = CityLocationUtils.getLocationIdByCityName(cityName);
        log.info("城市名称转换为Location_ID: cityName={}, locationId={}", cityName, locationId);
        
        return locationId;
    }
    
    /**
     * 根据客户端IP解析城市名称
     * 
     * @return 城市名称
     */
    private String resolveCityNameByIp()
    {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String clientIp = IpUtils.getIpAddr(request);
                log.info("客户端IP地址: {}", clientIp);
                
                String cityName = IpLocationUtils.getCityNameByIp(clientIp);
                if (StringUtils.hasText(cityName)) {
                    log.info("根据IP解析的城市: {}", cityName);
                    return cityName;
                }
                
                log.warn("IP地址解析城市失败，使用默认城市: 广州");
            } else {
                log.warn("无法获取请求上下文，使用默认城市: 广州");
            }
        } catch (Exception e) {
            log.error("IP地址解析异常: {}", e.getMessage(), e);
        }
        
        return "广州";  // 默认城市
    }

    /**
     * 调用和风天气API
     * 
     * @param location 城市位置
     * @param lang 语言
     * @param unit 单位
     * @return API响应
     * @throws Exception API调用异常
     */
    private ResponseEntity<QWeatherResponseVo<WeatherNowVo>> callWeatherApi(String location, String lang, String unit) throws Exception
    {
        String jwtToken = QWeatherJwtUtil.generateToken(keyId, projectId, privateKey, 900); // 15分钟有效期
        
        String url = UriComponentsBuilder
                .fromHttpUrl("https://" + weatherApiHost + "/v7/weather/now")
                .queryParam("location", location)
                .queryParam("lang", lang)
                .queryParam("unit", unit)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtToken);
        headers.set("Accept", "application/json");
        headers.set("Accept-Encoding", "gzip");

        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.info("调用和风天气API: location={}, lang={}, unit={}, url={}", location, lang, unit, url);
        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<QWeatherResponseVo<WeatherNowVo>>() {}
        );
    }

    /**
     * 处理和风天气API响应
     * 
     * @param response API响应
     * @param cacheKey 缓存key
     * @param location 城市位置
     * @return 处理结果
     */
    private ResponseResult handleWeatherResponse(ResponseEntity<QWeatherResponseVo<WeatherNowVo>> response, 
                                                  String cacheKey, String location)
    {
        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            log.error("调用和风天气API失败: status={}", response.getStatusCode());
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), "获取天气信息失败");
        }

        QWeatherResponseVo<WeatherNowVo> weatherResponse = response.getBody();
        
        if (!"200".equals(weatherResponse.getCode())) {
            log.error("和风天气API返回错误: code={}", weatherResponse.getCode());
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(),
                    "获取天气信息失败，错误码：" + weatherResponse.getCode());
        }

        WeatherNowVo weatherNow = weatherResponse.getNow();
        
        // 设置天气类型（根据text字段判断）
        WeatherTypeUtils.WeatherType weatherType = WeatherTypeUtils.getWeatherType(weatherNow.getText());
        weatherNow.setWeatherType(weatherType.getValue());
        log.info("天气类型判断: text={}, weatherType={}", weatherNow.getText(), weatherType.getValue());
        
        // 缓存数据
        redisCache.setCacheObject(cacheKey, weatherNow, cacheMinutes, TimeUnit.MINUTES);
        log.info("天气数据已缓存: location={}, cacheMinutes={}", location, cacheMinutes);

        return ResponseResult.okResult(weatherNow);
    }
}
