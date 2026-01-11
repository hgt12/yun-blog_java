package com.mengze.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

//用VO类来规范多少字段可以传到前端
//在项目中要把VO来接受查询出来的结果用一个接口对应一个VO
//这样即使接口响应字段要修改也只要改VO即可
//在这里你想要传去前端哪些字段就选择哪些字段传就行
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryVo
{
    private Long id;
    //分类名
    private String name;
    //分类描述
    private String description;
    //文章数量
    private Integer articleCount;
    //状态
    private String status;
}
