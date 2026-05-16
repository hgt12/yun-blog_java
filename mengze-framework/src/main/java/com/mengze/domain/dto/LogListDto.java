package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

/**
 * 日志查询请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "日志查询请求对象")
public class LogListDto {

    @ApiModelProperty(value = "关键字（用于模糊搜索）", example = "日志")
    private String keywords;

    @ApiModelProperty(value = "开始时间", example = "2026-04-01 00:00:00")
    private String startTime;

    @ApiModelProperty(value = "结束时间", example = "2026-04-30 23:59:59")
    private String endTime;
}
