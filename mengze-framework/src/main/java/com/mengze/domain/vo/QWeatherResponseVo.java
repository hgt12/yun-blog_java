package com.mengze.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 和风天气API响应包装类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "和风天气API响应对象")
public class QWeatherResponseVo<T> implements Serializable {

    @ApiModelProperty(value = "状态码", example = "200")
    @JsonProperty("code")
    private String code;

    @ApiModelProperty(value = "天气数据更新时间", example = "2024-01-01T12:00+08:00")
    @JsonProperty("updateTime")
    private String updateTime;

    @ApiModelProperty(value = "当前数据的响应式页面，便于嵌入网站或应用", example = "https://www.qweather.com/weather/beijing-101010100.html")
    @JsonProperty("fxLink")
    private String fxLink;

    @ApiModelProperty(value = "实时天气数据")
    @JsonProperty("now")
    private T now;

    @ApiModelProperty(value = "数据来源和许可信息")
    @JsonProperty("refer")
    private Refer refer;

    @Data
    public static class Refer implements Serializable {
        @ApiModelProperty(value = "数据来源", example = "[\"QWeather\"]")
        @JsonProperty("sources")
        private String[] sources;

        @ApiModelProperty(value = "许可证", example = "[\"QWeather Developers License\"]")
        @JsonProperty("license")
        private String[] license;
    }
}
