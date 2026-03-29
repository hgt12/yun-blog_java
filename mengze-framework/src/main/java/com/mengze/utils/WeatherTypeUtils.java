package com.mengze.utils;

/**
 * 天气类型工具类
 * 根据和风天气API返回的天气文本描述判断天气类型
 */
public class WeatherTypeUtils
{

    /**
     * 天气类型枚举
     */
    public enum WeatherType {
        SUNNY("sunny"),       // 晴天
        OVERCAST("overcast"), // 阴天
        RAINING("raining"),   // 下雨
        SNOWING("snowing"),   // 下雪
        FOG("fog");           // 雾/霾

        private final String value;

        WeatherType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 根据天气文本描述判断天气类型
     * 
     * @param weatherText 和风天气API返回的天气文本（如：晴、多云、小雨、大雪等）
     * @return 天气类型
     */
    public static WeatherType getWeatherType(String weatherText) {
        if (weatherText == null || weatherText.trim().isEmpty()) {
            return WeatherType.SUNNY; // 默认返回晴天
        }

        // 下雪类型判断
        if (weatherText.contains("雪") || weatherText.contains("冰雹") || 
            weatherText.contains("雨夹雪") || weatherText.contains("冻雨") ||
            weatherText.contains("阵雨夹雪") || weatherText.contains("雨雪天气")) {
            return WeatherType.SNOWING;
        }

        // 下雨类型判断
        if (weatherText.contains("雨") || weatherText.contains("雷") || 
            weatherText.contains("阵雨") || weatherText.contains("毛毛雨")) {
            return WeatherType.RAINING;
        }

        // 阴天类型判断
        if (weatherText.contains("阴") || weatherText.contains("多云") || 
            weatherText.contains("少云")) {
            return WeatherType.OVERCAST;
        }

        // 雾/霾天类型判断
        if (weatherText.contains("雾") || weatherText.contains("霾") ||
            weatherText.contains("扬沙") || weatherText.contains("浮尘") ||
            weatherText.contains("沙尘暴")) {
            return WeatherType.FOG;
        }

        // 晴天类型判断（包括晴、晴间多云等）
        return WeatherType.SUNNY;
    }
}
