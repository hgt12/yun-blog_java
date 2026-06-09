package com.mengze.controller;

import com.mengze.annotation.SystemLog;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.vo.WeatherNowVo;
import com.mengze.service.WeatherService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping()
@Api(tags = "天气")
public class WeatherController
{

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/weather/current")
    @SystemLog(businessName = "获取天气")
    @ApiOperation(value = "获取天气", notes = "根据位置获取实时天气信息，如果不传location则自动根据IP定位，和后台才不多，前台主要用于触发特定组件用到", response = WeatherNowVo.class)
    public ResponseResult<?> getWeatherCurrent(
            @ApiParam(value = "位置ID或经纬度，不传则自动根据IP定位")
            @RequestParam(required = false) String location,
            @ApiParam(value = "语言设置，默认zh")
            @RequestParam(required = false, defaultValue = "zh") String lang,
            @ApiParam(value = "单位制式，m=公制，i=英制，默认m")
            @RequestParam(required = false, defaultValue = "m") String unit) {
        return weatherService.getWeatherNow(location, lang, unit);
    }
}

