package com.mengze.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 天气数据响应VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "实时天气信息")
public class WeatherNowVo implements Serializable {

    @ApiModelProperty(value = "数据观测时间")
    @JsonProperty("obsTime")
    private String obsTime;

    @ApiModelProperty(value = "温度，默认单位：摄氏度")
    @JsonProperty("temp")
    private String temp;

    @ApiModelProperty(value = "体感温度，默认单位：摄氏度")
    @JsonProperty("feelsLike")
    private String feelsLike;

    @ApiModelProperty(value = "天气状况的图标代码")
    @JsonProperty("icon")
    private String icon;

    @ApiModelProperty(value = "天气状况的文字描述")
    @JsonProperty("text")
    private String text;

    @ApiModelProperty(value = "风向360角度")
    @JsonProperty("wind360")
    private String wind360;

    @ApiModelProperty(value = "风向")
    @JsonProperty("windDir")
    private String windDir;

    @ApiModelProperty(value = "风力等级")
    @JsonProperty("windScale")
    private String windScale;

    @ApiModelProperty(value = "风速，公里/小时")
    @JsonProperty("windSpeed")
    private String windSpeed;

    @ApiModelProperty(value = "相对湿度，百分比数值")
    @JsonProperty("humidity")
    private String humidity;

    @ApiModelProperty(value = "当前小时累计降水量，默认单位：毫米")
    @JsonProperty("precip")
    private String precip;

    @ApiModelProperty(value = "大气压强，默认单位：百帕")
    @JsonProperty("pressure")
    private String pressure;

    @ApiModelProperty(value = "能见度，默认单位：公里")
    @JsonProperty("vis")
    private String vis;

    @ApiModelProperty(value = "云量，百分比数值")
    @JsonProperty("cloud")
    private String cloud;

    @ApiModelProperty(value = "露点温度")
    @JsonProperty("dew")
    private String dew;

    @ApiModelProperty(value = "天气类型（前端使用）")
    @JsonProperty("weatherType")
    private String weatherType;
}
