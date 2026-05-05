package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网站统计信息VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "网站统计信息")
public class SiteInfoVo {

    @ApiModelProperty(value = "总访问量（PV）", example = "10000")
    private Long totalViews;

    @ApiModelProperty(value = "总访客数（UV）", example = "5000")
    private Long totalVisitors;

    @ApiModelProperty(value = "文章总数", example = "100")
    private Integer articleCount;

    @ApiModelProperty(value = "分类总数", example = "10")
    private Integer categoryCount;

    @ApiModelProperty(value = "标签总数", example = "20")
    private Integer tagCount;
}
