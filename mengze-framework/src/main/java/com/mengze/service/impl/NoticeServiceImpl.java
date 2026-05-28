package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.SysNotice;
import com.mengze.domain.entity.SysNoticeUser;
import com.mengze.domain.entity.SysUser;
import com.mengze.domain.vo.BlogNoticeListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.mapper.SysNoticeMapper;
import com.mengze.service.NoticeService;
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
 * 系统通知公告表(SysNotice)表服务实现类(前台)
 *
 * @author makejava
 * @since 2026-05-28 10:19:37
 */
@Service("noticeService")
public class NoticeServiceImpl extends ServiceImpl<SysNoticeMapper, SysNotice> implements NoticeService
{
    @Autowired
    private SysNoticeUserService sysNoticeUserService;

    @Autowired
    private SysUserService sysUserService;

    //获取通知列表
    @Override
    public ResponseResult<PageVo> getNoticeList(Integer pageNum, Integer pageSize)
    {
        Long userId = SecurityUtils.getUserId();

        //前台仅展示已发布通知；未登录用户只能看全体通知，登录用户还能看发给自己的指定通知
        LambdaQueryWrapper<SysNotice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysNotice::getStatus, SystemConstants.NOTICE_STATUS_PUBLISHED)
                .eq(SysNotice::getDelFlag, SystemConstants.STATUS_NORMAL);
        if (userId == null) {
            queryWrapper.eq(SysNotice::getTargetType, SystemConstants.TARGET_TYPE_IS_ALL);
        } else {
            queryWrapper.and(wrapper -> wrapper
                    .eq(SysNotice::getTargetType, SystemConstants.TARGET_TYPE_IS_ALL)
                    .or(orWrapper -> orWrapper
                            .eq(SysNotice::getTargetType, SystemConstants.TARGET_TYPE_IS_SPECIFIED)
                            .apply("find_in_set({0}, target_user_ids)", userId)));
        }

        //按发布时间倒序排序
        queryWrapper.orderByDesc(SysNotice::getPublishTime);

        //分页查询
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        this.page(page, queryWrapper);

        List<SysNotice> noticeList = page.getRecords();

        //实体转换为Vo
        List<BlogNoticeListVo> noticeListVos = BeanCopyUtils.copyBeanList(noticeList, BlogNoticeListVo.class);

        //查询发布人的名字并回填
        List<Long> publisherIds = noticeList.stream()
                .map(SysNotice::getPublisherId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> publisherNameMap = new HashMap<>();
        if (!publisherIds.isEmpty()) {
            List<SysUser> publisherList = sysUserService.listByIds(publisherIds);
            publisherNameMap = publisherList.stream()
                    .collect(Collectors.toMap(SysUser::getId, user -> StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername(), (oldVal, newVal) -> oldVal));
        }

        //查询指定接收人的名字并回填
        Map<Long, String> targetUserNameMap = new HashMap<>();
        List<Long> targetUserIds = noticeList.stream()
                .filter(notice -> SystemConstants.TARGET_TYPE_IS_SPECIFIED.equals(notice.getTargetType()))
                .map(SysNotice::getTargetUserIds)
                .filter(StringUtils::hasText)
                .flatMap(targetUserIdsStr -> java.util.Arrays.stream(targetUserIdsStr.split(",")))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(Long::valueOf)
                .distinct()
                .collect(Collectors.toList());
        if (!targetUserIds.isEmpty()) {
            List<SysUser> targetUserList = sysUserService.listByIds(targetUserIds);
            targetUserNameMap = targetUserList.stream()
                    .collect(Collectors.toMap(SysUser::getId, user -> StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername(), (oldVal, newVal) -> oldVal));
        }

        //将接收人名字集合封装进 VO
        for (int i = 0; i < noticeList.size(); i++)
        {
            SysNotice notice = noticeList.get(i);
            BlogNoticeListVo noticeListVo = noticeListVos.get(i);
            noticeListVo.setPublisherName(publisherNameMap.get(notice.getPublisherId()));
            noticeListVo.setTargetUserIds(notice.getTargetUserIds());
            if (SystemConstants.TARGET_TYPE_IS_SPECIFIED.equals(notice.getTargetType()) && StringUtils.hasText(notice.getTargetUserIds())) {
                List<String> targetNames = java.util.Arrays.stream(notice.getTargetUserIds().split(","))
                        .map(String::trim)
                        .filter(StringUtils::hasText)
                        .map(Long::valueOf)
                        .map(targetUserNameMap::get)
                        .filter(StringUtils::hasText)
                        .collect(Collectors.toList());
                noticeListVo.setTargetName(targetNames);
            }
        }

        //默认未读
        for (BlogNoticeListVo noticeListVo : noticeListVos) {
            noticeListVo.setIsRead(SystemConstants.NOTICE_NOT_READ);
        }

        //如果当前用户已登录，再查询并回填阅读状态
        if (userId != null && !noticeList.isEmpty()) {
            List<Long> noticeIds = noticeList.stream().map(SysNotice::getId).collect(Collectors.toList());
            List<SysNoticeUser> noticeUserList = sysNoticeUserService.list(new LambdaQueryWrapper<SysNoticeUser>()
                    .eq(SysNoticeUser::getUserId, userId)
                    .in(SysNoticeUser::getNoticeId, noticeIds));

            Map<Long, String> noticeReadMap = noticeUserList.stream()
                    .collect(Collectors.toMap(SysNoticeUser::getNoticeId, SysNoticeUser::getIsRead, (oldVal, newVal) -> oldVal));

            for (BlogNoticeListVo noticeListVo : noticeListVos) {
                noticeListVo.setIsRead(noticeReadMap.getOrDefault(noticeListVo.getId(), SystemConstants.NOTICE_NOT_READ));
            }
        }

        PageVo pageVo = new PageVo(noticeListVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
    }
}

