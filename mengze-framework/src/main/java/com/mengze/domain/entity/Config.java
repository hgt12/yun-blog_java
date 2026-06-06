package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 系统配置表(Config)表实体类
 *
 * @author makejava
 * @since 2026-06-06 14:22:06
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_config")
public class Config extends Model<Config> {
    @TableId

    private Long id;
    //配置名称
    private String configName;
    //配置键
    private String configKey;
    //配置值
    private String configValue;
    //备注
    private String remark;
    //排序
    private Integer sort;
    //配置分类（0-前台配置，1-后台配置）
    private String configType;
    //删除标记(0-未删除,1-已删除)
    private String delFlag;
    //创建者ID
    private Long createBy;
    //创建时间
    private Date createTime;
    //更新者ID
    private Long updateBy;
    //更新时间
    private Date updateTime;

}

