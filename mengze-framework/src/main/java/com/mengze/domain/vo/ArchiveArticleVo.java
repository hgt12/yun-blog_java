package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "归档文章响应对象")
public class ArchiveArticleVo {

    @ApiModelProperty(value = "文章ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "文章标题", example = "Spring Boot入门教程")
    private String title;

    @ApiModelProperty(value = "创建时间", example = "2024-01-01 12:00:00")
    private String createTime;
}
