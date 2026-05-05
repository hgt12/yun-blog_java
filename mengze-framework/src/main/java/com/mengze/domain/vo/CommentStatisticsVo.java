package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论统计信息VO
 *
 * @author mengze
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "评论统计信息响应对象")
public class CommentStatisticsVo {
    
    @ApiModelProperty(value = "评论总数", example = "1000")
    private Long total;
    
    @ApiModelProperty(value = "待审核评论数", example = "10")
    private Long pendingReview;
    
    @ApiModelProperty(value = "今日评论数", example = "5")
    private Long todayCount;
    
    @ApiModelProperty(value = "审核未通过评论数", example = "2")
    private Long rejected;
}
