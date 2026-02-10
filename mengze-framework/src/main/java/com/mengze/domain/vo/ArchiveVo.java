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
    /**
     * 年份
     */
    @ApiModelProperty(value = "年份")
    private Integer year;

    /**
     * 月份
     */
    @ApiModelProperty(value = "月份")
    private Integer month;

    /**
     * 该月文章数量
     */
    @ApiModelProperty(value = "该月文章数量")
    private Integer articleCount;

    /**
     * 文章列表
     */
    @ApiModelProperty(value = "文章列表")
    private List<ArchiveArticleVo> articles;
}
