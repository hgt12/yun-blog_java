package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * 记录访问请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "记录访问请求对象")
public class RecordVisitDto {

    @NotBlank(message = "页面URL不能为空")
    @ApiModelProperty(value = "访问的页面URL", required = true, example = "/article/1")
    private String pageUrl;

    @ApiModelProperty(value = "页面标题", example = "文章标题")
    private String pageTitle;

    @ApiModelProperty(value = "来源页面", example = "https://www.google.com")
    private String referrer;
}
