package com.mengze.domain.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传文件的元数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadFileMetaVo {

    @ApiModelProperty(value = "文件名", example = "fd2c11e91a984e80a80004a22ca6707d_avatar.png")
    private String name;

    @ApiModelProperty(value = "文件路径", example = "2026/05/07/fd2c11e91a984e80a80004a22ca6707d_avatar.png")
    private String filePath;

    @ApiModelProperty(value = "标签ID", example = "http://t8ul2nlc8.hn-bkt.clouddn.com/2026/05/07/fd2c11e91a984e80a80004a22ca6707d_avatar.png")
    private String url;
}
