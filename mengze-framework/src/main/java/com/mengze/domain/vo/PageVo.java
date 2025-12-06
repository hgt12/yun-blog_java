package com.mengze.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

//分页Vo
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageVo
{
    private List rows;
    //总记录数，方面页面根据其判断是否请求传递下一页
    private Long total;
}
