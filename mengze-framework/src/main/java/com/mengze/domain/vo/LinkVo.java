package com.mengze.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LinkVo
{
    //id
    private Long id;
    //友链名字
    private String name;
    //头像
    private String logo;
    //个人描述
    private String description;
    //网站地址
    private String address;
}
