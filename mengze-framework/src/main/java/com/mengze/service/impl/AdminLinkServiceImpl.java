package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.LinkListDto;
import com.mengze.domain.entity.Link;
import com.mengze.domain.vo.AdminLinkListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.service.AdminLinkService;
import com.mengze.service.LinkService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 后台友链管理服务实现类
 *
 * @author mengze
 */
@Service
public class AdminLinkServiceImpl implements AdminLinkService
{
    @Autowired
    LinkService linkService;

    //获取友链列表
    @Override
    public ResponseResult linkList(Integer pageNum, Integer pageSize, LinkListDto linkListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<Link> queryWrapper = new LambdaQueryWrapper<>();

        //根据关键字模糊查询友链名称
        if(StringUtils.hasText(linkListDto.getKeywords())) {
            queryWrapper.like(Link::getName, linkListDto.getKeywords());
        }

        //根据状态筛选
        if (StringUtils.hasText(linkListDto.getStatus())){
            queryWrapper.eq(Link::getStatus, linkListDto.getStatus());
        }

        //先以排序序号升序排序和序号相同时按创建时间降序排序
        queryWrapper.orderByAsc(Link::getSort)
                    .orderByDesc(Link::getCreateTime);

        //分页查询
        Page<Link> page = new Page<>(pageNum, pageSize);
        linkService.page(page, queryWrapper);

        //封装成vo并返回
        List<AdminLinkListVo> linkVo = BeanCopyUtils.copyBeanList(page.getRecords(), AdminLinkListVo.class);
        PageVo pageVo = new PageVo(linkVo, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }
}
