package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.NoticeListDto;
import com.mengze.domain.entity.SysDict;
import com.mengze.domain.entity.SysNotice;
import com.mengze.domain.entity.SysNoticeUser;
import com.mengze.domain.entity.SysUser;
import com.mengze.domain.vo.DictFormDetailVo;
import com.mengze.domain.vo.NoticeFormDetailVo;
import com.mengze.domain.vo.NoticeListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysNoticeMapper;
import com.mengze.service.SysNoticeService;
import com.mengze.service.SysNoticeUserService;
import com.mengze.service.SysUserService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 通知公告表(SysNotice)表服务实现类
 *
 * @author makejava
 * @since 2026-05-25 20:30:59
 */
@Service("sysNoticeService")
public class SysNoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements SysNoticeService
{
    @Autowired
    private SysNoticeUserService sysNoticeUserService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private SysNoticeService sysNoticeService;

    //查询通知列表
    @Override
    public ResponseResult<PageVo> getNoticeList(Integer pageNum, Integer pageSize, NoticeListDto noticeListDto)
    {
        //获取当前登录用户ID 用于过滤该用户可见的通知
        Long userId = SecurityUtils.getUserId();

        //构建查询条件
        LambdaQueryWrapper<SysNotice> queryWrapper = new LambdaQueryWrapper<>();

        //根据通知标题模糊搜索
        if (StringUtils.hasText(noticeListDto.getTitle())) {
            queryWrapper.like(SysNotice::getTitle, noticeListDto.getTitle());
        }

        //根据状态筛选
        if (StringUtils.hasText(noticeListDto.getStatus())) {
            queryWrapper.eq(SysNotice::getStatus, noticeListDto.getStatus());
        }

        //根据当前用户过滤可见通知：
        //targetType=1 表示全体可见
        //targetType=2 时，通过 targetUserIds 判断是否包含当前用户ID
        if (userId != null) {
            queryWrapper.and(wrapper -> wrapper.eq(SysNotice::getTargetType,  SystemConstants.TARGET_TYPE_IS_ALL)
                    .or()
                    .like(SysNotice::getTargetUserIds, userId.toString()));
        } else {
            //未登录时仅展示全体通知
            queryWrapper.eq(SysNotice::getTargetType, SystemConstants.TARGET_TYPE_IS_ALL);
        }

        //按id顺序排序
        queryWrapper.orderByAsc(SysNotice::getId);

        //分页查询
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        this.page(page, queryWrapper);

        List<SysNotice> noticeList = page.getRecords();

        //封装查询结果为Vo返回
        List<NoticeListVo> noticeListVos = BeanCopyUtils.copyBeanList(noticeList, NoticeListVo.class);

        //查询本页通知的 publisherId再批量查询发布人信息
        List<Long> publisherIds = noticeList.stream()
                .map(SysNotice::getPublisherId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> publisherNameMap = new HashMap<>();
        if (!publisherIds.isEmpty()) {
            List<SysUser> publisherList = sysUserService.listByIds(publisherIds);
            publisherNameMap = publisherList.stream()
                    .collect(Collectors.toMap(SysUser::getId, SysUser::getNickname, (oldVal, newVal) -> oldVal));
        }

        //把发布人名称收集到返回结果中
        for (int i = 0; i < noticeList.size(); i++) {
            SysNotice notice = noticeList.get(i);
            NoticeListVo noticeListVo = noticeListVos.get(i);
            noticeListVo.setPublisherName(publisherNameMap.get(notice.getPublisherId()));
        }

        //查询当前用户对这些通知的阅读状态
        if (userId != null && !noticeList.isEmpty()) {
            List<Long> noticeIds = noticeList.stream().map(SysNotice::getId).collect(Collectors.toList());
            List<SysNoticeUser> noticeUserList = sysNoticeUserService.list(new LambdaQueryWrapper<SysNoticeUser>()
                    .eq(SysNoticeUser::getUserId, userId)
                    .in(SysNoticeUser::getNoticeId, noticeIds));

            Map<Long, String> noticeReadMap = noticeUserList.stream()
                    .collect(Collectors.toMap(SysNoticeUser::getNoticeId, SysNoticeUser::getIsRead, (oldVal, newVal) -> oldVal));

            for (NoticeListVo noticeListVo : noticeListVos) {
                noticeListVo.setIsRead(noticeReadMap.getOrDefault(noticeListVo.getId(), SystemConstants.NOTICE_NOT_READ));
            }
        } else {
            //未登录时默认未读
            for (NoticeListVo noticeListVo : noticeListVos) {
                noticeListVo.setIsRead(SystemConstants.NOTICE_NOT_READ);
            }
        }

        PageVo pageVo = new PageVo(noticeListVos, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }

    //获取通知表单数据
    @Override
    public ResponseResult<NoticeFormDetailVo> getNoticeFormDetail(Long id)
    {
        //根据通知id 查询通知详情
        SysNotice notice = sysNoticeService.getById(id);
        if (notice == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该通知不存在！");
        }

        //转换成Vo
        NoticeFormDetailVo noticeFormDetailVo = BeanCopyUtils.copyBean(notice, NoticeFormDetailVo.class);

        //返回
        return ResponseResult.okResult(noticeFormDetailVo);
    }
}

