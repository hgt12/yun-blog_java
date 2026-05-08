package com.mengze.domain.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Pattern;

/**
 * 文件查询请求DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "文件查询请求对象")
public class FileListDto {

    @ApiModelProperty(value = "类型", example = "图片")
    private String type;

    @ApiModelProperty(value = "关键字（用于模糊搜索文件名称）", example = "Fluid")
    private String keywords;
}
