package com.mengze.domain.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "文件列表响应对象")
public class FileListVo
{
    @ApiModelProperty(value = "文件ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "文件名称", example = "bb278d771b9c47b9ab48843f554447ff_loading_1.gif")
    private String name;

    @ApiModelProperty(value = "文件来源", example = "用户“里斯啊”的头像图片")
    private String fileSource;

    @ApiModelProperty(value = "文件类型（图片，视频，文档等....）", example = "图片")
    private String type;

    @ApiModelProperty(value = "文件存储路径", example = "文件oss中的存储路径")
    private String filePath;

    @ApiModelProperty(value = "文件URL，图片预览URL", example = "域名 + 文件oss中的存储路径")
    private String url;

    @ApiModelProperty(value = "文件大小(字节)", example = "363856")
    private Long size;

    @ApiModelProperty(value = "MIME类型", example = "image/gif")
    private String mimeType;

    @ApiModelProperty(value = "创建者ID", example = "1")
    private Long createBy;

    @ApiModelProperty(value = "创建时间", example = "2026-05-08 14:40:22")
    private Date createTime;

    @ApiModelProperty(value = "更新时间", example = "2026-05-08 14:40:50")
    private Date updateTime;
}
