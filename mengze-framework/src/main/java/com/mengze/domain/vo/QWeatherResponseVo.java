package com.mengze.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 和风天气API响应包装类
 */
@Data
@ApiModel(description = "和风天气API响应对象")
public class QWeatherResponseVo<T> implements Serializable {

    @ApiModelProperty(value = "状态码")
    @JsonProperty("code")
    private String code;

    @ApiModelProperty(value = "天气数据更新时间")
    @JsonProperty("updateTime")
    private String updateTime;

    @ApiModelProperty(value = "当前数据的响应式页面，便于嵌入网站或应用")
    @JsonProperty("fxLink")
    private String fxLink;

    @ApiModelProperty(value = "now")
    @JsonProperty("now")
    private T now;

    @ApiModelProperty(value = "refer")
    @JsonProperty("refer")
    private Refer refer;

    @Data
    public static class Refer implements Serializable {
        @ApiModelProperty(value = "数据来源")
        @JsonProperty("sources")
        private String[] sources;

        @ApiModelProperty(value = "许可证")
        @JsonProperty("license")
        private String[] license;
    }
}
