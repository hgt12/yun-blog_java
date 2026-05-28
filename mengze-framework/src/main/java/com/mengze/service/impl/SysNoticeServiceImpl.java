package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddNoticeDto;
import com.mengze.domain.dto.NoticeListDto;
import com.mengze.domain.entity.*;
import com.mengze.domain.vo.NoticeFormDetailVo;
import com.mengze.domain.vo.NoticeListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.domain.vo.UploadFileMetaVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysNoticeMapper;
import com.mengze.service.OssFileService;
import com.mengze.service.SysFileService;
import com.mengze.service.SysNoticeService;
import com.mengze.service.SysNoticeUserService;
import com.mengze.service.SysUserService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.ImageUrlUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
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

    @Autowired
    private OssFileService ossFileService;

    @Autowired
    private SysFileService sysFileService;

    //查询通知列表
    @Override
    public ResponseResult<PageVo> getNoticeList(Integer pageNum, Integer pageSize, NoticeListDto noticeListDto)
    {
        //获取当前登录用户ID和角色信息
        Long userId = SecurityUtils.getUserId();
        boolean isAdmin = SecurityUtils.isAdmin();

        //构建查询条件
        LambdaQueryWrapper<SysNotice> queryWrapper = new LambdaQueryWrapper<>();

        //根据通知标题模糊搜索
        if (noticeListDto != null && StringUtils.hasText(noticeListDto.getTitle())) {
            queryWrapper.like(SysNotice::getTitle, noticeListDto.getTitle());
        }

        //管理员可按前端传入状态筛选，普通用户仅能查看已发布通知
        if (isAdmin) {
            if (noticeListDto != null && StringUtils.hasText(noticeListDto.getStatus())) {
                queryWrapper.eq(SysNotice::getStatus, noticeListDto.getStatus());
            }
        } else {
            queryWrapper.eq(SysNotice::getStatus, SystemConstants.NOTICE_STATUS_PUBLISHED);
        }

        //管理员可查看所有通知，普通用户只能查看全体通知和自己的通知
        if (!isAdmin) {
            queryWrapper.and(wrapper -> wrapper.eq(SysNotice::getTargetType, SystemConstants.TARGET_TYPE_IS_ALL)
                    .or()
                    .apply("concat(',', target_user_ids, ',') like {0}", "%," + userId + ",%"));
        }

        //按发布时间倒序排序
        queryWrapper.orderByDesc(SysNotice::getPublishTime);

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

        //把发布人名称回填到返回结果中
        for (int i = 0; i < noticeList.size(); i++) {
            SysNotice notice = noticeList.get(i);
            NoticeListVo noticeListVo = noticeListVos.get(i);
            noticeListVo.setPublisherName(publisherNameMap.get(notice.getPublisherId()));
        }

        //默认都设置为未读
        for (NoticeListVo noticeListVo : noticeListVos) {
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

            for (NoticeListVo noticeListVo : noticeListVos) {
                noticeListVo.setIsRead(noticeReadMap.getOrDefault(noticeListVo.getId(), SystemConstants.NOTICE_NOT_READ));
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

    //添加通知
    @Override
    @Transactional
    public ResponseResult addNotice(AddNoticeDto addNoticeDto)
    {
        //只处理通知内容中的临时图片，将其转为正式文件
        String content = addNoticeDto.getContent();
        if (ImageUrlUtils.containsTempImages(content)) {
            content = processContentImages(content, addNoticeDto.getTitle());
            addNoticeDto.setContent(content);
        }

        //封装通知实体
        SysNotice sysNotice = BeanCopyUtils.copyBean(addNoticeDto, SysNotice.class);
        sysNotice.setTargetUserIds(convertTargetUserIds(addNoticeDto.getTargetUserIds()));
        sysNotice.setPublisherId(SecurityUtils.getUserId());
        sysNotice.setPublishTime(new Date());

        //保存通知
        boolean save = this.save(sysNotice);
        if (!save) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "添加通知失败");
        }

        return ResponseResult.okResult();
    }

    //将通知内容中的临时图片转正并同步文件表
    private String processContentImages(String content, String noticeTitle)
    {
        List<String> tempImageUrls = ImageUrlUtils.extractTempImageUrls(content);
        String result = content;

        //同一通知里可能重复引用同一张临时图片，先去重
        List<String> distinctTempImageUrls = tempImageUrls.stream().distinct().toList();

        for (String tempUrl : distinctTempImageUrls) {
            UploadFileMetaVo uploadFileMetaVo = ossFileService.moveTempToFormal(tempUrl);
            if (uploadFileMetaVo == null || !StringUtils.hasText(uploadFileMetaVo.getUrl())) {
                throw new RuntimeException("通知内容图片保存失败，无法保存通知记录: " + tempUrl);
            }

            result = ImageUrlUtils.replaceImageUrl(result, tempUrl, uploadFileMetaVo.getUrl());

            if (StringUtils.hasText(uploadFileMetaVo.getFilePath())) {
                saveFileRecord(uploadFileMetaVo.getName(),
                        uploadFileMetaVo.getFilePath(),
                        uploadFileMetaVo.getUrl(),
                        uploadFileMetaVo.getSize(),
                        uploadFileMetaVo.getMimeType(),
                        "通知《" + noticeTitle + "》中的图片");
            }
        }

        return result;
    }

    //把目标用户ID列表转换成数据库存储格式
    private String convertTargetUserIds(List<Long> targetUsers)
    {
        if (targetUsers == null || targetUsers.isEmpty()) {
            return null;
        }
        return targetUsers.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    //将元数据写入文件表
    private void saveFileRecord(String fileName, String filePath, String url, Long size, String mimeType, String fileSource)
    {
        SysFile sysFile = new SysFile();
        sysFile.setName(fileName);
        sysFile.setFilePath(filePath);
        sysFile.setUrl(url);
        sysFile.setSize(size);
        sysFile.setMimeType(mimeType);
        sysFile.setFileSource(fileSource);
        sysFileService.save(sysFile);
    }
}

