package com.mengze.domain.entity;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色表(SysRole)表实体类
 *
 * @author makejava
 * @since 2026-03-27 19:27:13
 */
@SuppressWarnings("serial")
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("sys_role")
public class SysRole extends Model<SysRole> {
    @TableId

    private Long id;
    //角色编码
    private String role_code;
    //角色名称
    private String name;
    //排序
    private Integer sort;
    //状态(0-停用,1-启用)
    private String status;
    //数据权限(1-所有数据 2-部门及子部门数据 3-本部门数据 4-本人数据 5-自定义部门数据)
    private Integer dataScope;
    //备注
    private String remark;
    //删除标记(0-未删除,1-已删除)
    private String delFlag;
    //创建时间
    private Date createTime;
    //更新时间
    private Date updateTime;
    //创建者ID
    private Long createBy;
    //更新者ID
    private Long updateBy;

}

