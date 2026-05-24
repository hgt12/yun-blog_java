package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 日志查询请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "日志查询请求对象")
public class LogListDto {

    @ApiModelProperty(value = "关键字（操作人或页面标题）", example = "日志")
    private String keywords;

    @ApiModelProperty(value = "访问时间", example = "[2026-04-01, 2026-05-15]")
    private List<String> visitTime;
}
