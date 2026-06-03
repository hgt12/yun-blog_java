package com.mengze.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 在线用户数量信息Vo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineCountVo implements Serializable
{
    //当前在线用户数
    private Long count;

    //服务端更新时间戳
    private Long timestamp;
}
