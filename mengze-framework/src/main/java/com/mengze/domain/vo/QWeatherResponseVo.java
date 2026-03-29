package com.mengze.domain.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 和风天气API响应包装类
 */
@Data
public class QWeatherResponseVo<T> implements Serializable {

    @JsonProperty("状态码")
    private String code;

    @JsonProperty("天气数据更新时间")
    private String updateTime;

    @JsonProperty("当前数据的响应式页面，便于嵌入网站或应用")
    private String fxLink;

    @JsonProperty("now")
    private T now;

    @JsonProperty("refer")
    private Refer refer;

    @Data
    public static class Refer implements Serializable {
        @JsonProperty("数据来源")
        private String[] sources;

        @JsonProperty("许可证")
        private String[] license;
    }
}
