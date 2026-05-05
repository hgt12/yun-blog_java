package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "归档响应对象")
public class ArchiveVo {

    @ApiModelProperty(value = "年份", example = "2024")
    private Integer year;

    @ApiModelProperty(value = "月份", example = "1")
    private Integer month;

    @ApiModelProperty(value = "该月文章数量", example = "10")
    private Integer articleCount;

    @ApiModelProperty(value = "文章列表")
    private List<ArchiveArticleVo> articles;
}
