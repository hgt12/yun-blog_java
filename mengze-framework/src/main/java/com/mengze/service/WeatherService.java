package com.mengze.service;

import com.mengze.domain.ResponseResult;

public interface WeatherService {
    ResponseResult getWeatherNow(String location, String lang, String unit);
}
