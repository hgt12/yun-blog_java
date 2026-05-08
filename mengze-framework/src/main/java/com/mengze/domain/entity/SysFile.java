package com.mengze.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * 上传文件元数据表(SysFile)表实体类
 *
 * @author makejava
 * @since 2026-05-07 19:28:10
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_file")
public class SysFile extends Model<SysFile> {
    @TableId
    //文件id
    private Long id;
    //文件名称
    private String name;
    //文件来源（例：文章图片，用户头像等....）
    private String fileSource;
    //文件类型
    private String type;
    //文件存储路径(用于删除)
    private String filePath;
    //文件URL，图片预览URL
    private String url;
    //文件大小(字节)
    private Long size;
    //MIME类型
    private String mimeType;
    //删除标记(0-未删除,1-已删除)
    private String delFlag;
    //创建者ID
    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
    //创建时间
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    //更新者ID
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;
    //更新时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


}

