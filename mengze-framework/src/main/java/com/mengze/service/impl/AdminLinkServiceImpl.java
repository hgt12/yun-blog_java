package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddLinkDto;
import com.mengze.domain.dto.LinkListDto;
import com.mengze.domain.dto.UpdateLinkDto;
import com.mengze.domain.entity.Link;
import com.mengze.domain.entity.Tag;
import com.mengze.domain.vo.AdminLinkListVo;
import com.mengze.domain.vo.LinkFormDetailVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.service.AdminLinkService;
import com.mengze.service.LinkService;
import com.mengze.utils.BeanCopyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
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
    private LinkService linkService;

    //获取友链列表
    @Override
    public ResponseResult getLinkList(Integer pageNum, Integer pageSize, LinkListDto linkListDto)
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

    //获取友链表单数据
    @Override
    public ResponseResult<LinkFormDetailVo> getLinkFormDetail(Long id)
    {
        //获取友链id
        Link link = linkService.getById(id);

        //封装为Vo
        LinkFormDetailVo linkFormDetailVo = BeanCopyUtils.copyBean(link, LinkFormDetailVo.class);

        //返回
        return ResponseResult.okResult(linkFormDetailVo);
    }

    //添加友链
    @Override
    public ResponseResult addLink(AddLinkDto addLinkDto)
    {
        // 检查站点地址是否已存在
        LambdaQueryWrapper<Link> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Link::getAddress, addLinkDto.getAddress());
        long count = linkService.count(queryWrapper);
        
        if (count > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该站点地址已存在，请勿重复添加！");
        }
        
        // 添加友链
        Link link = BeanCopyUtils.copyBean(addLinkDto, Link.class);
        linkService.save(link);
        return ResponseResult.okResult();
    }

    //更新友链
    @Override
    public ResponseResult updateLink(Long id, UpdateLinkDto updateLinkDto)
    {
        //获取友链id
        updateLinkDto.setId(id);

        // 检查友链站点地址是否与其他友链重复（排除自己）
        LambdaQueryWrapper<Link> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Link::getAddress, updateLinkDto.getName())
                .ne(Link::getId, id); // 排除当前的站点地址
        long count = linkService.count(queryWrapper);

        if (count > 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "站点地址已存在，请修改后重试！");
        }

        //更新友链信息
        Link link = BeanCopyUtils.copyBean(updateLinkDto, Link.class);
        linkService.updateById(link);

        return ResponseResult.okResult();
    }

    //删除友链
    @Override
    public ResponseResult deleteLink(Long[] ids)
    {
        // 判断是否有id
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的友链");
        }

        //获取友链id列表
        //因为友链不会与其他管理相关联
        //所以不需要进行像分类和标签删除功能的那些繁琐的检索
        List<Long> linkIdList = List.of(ids);
        linkService.removeByIds(linkIdList);

        return ResponseResult.okResult();
    }

}
