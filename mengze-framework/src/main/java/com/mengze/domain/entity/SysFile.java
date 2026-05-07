package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 上传文件元数据表(SysFile)表实体类
 *
 * @author makejava
 * @since 2026-05-07 09:15:48
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_file")
public class SysFile extends Model<SysFile> {
    @TableId

    private Long id;
    //文件名称
    private String name;
    //文件存储路径(用于删除)
    private String filePath;
    //文件URL
    private String url;
    //文件大小(字节)
    private Long size;
    //MIME类型
    private String mimeType;
    //文件扩展名
    private String ext;
    //删除标记(0-未删除,1-已删除)
    private String delFlag;
    //创建者ID
    private Long createBy;
    //创建时间
    private Date createTime;

}

