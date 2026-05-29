package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddNoticeDto;
import com.mengze.domain.dto.NoticeListDto;
import com.mengze.domain.dto.UpdateNoticeDto;
import com.mengze.domain.entity.*;
import com.mengze.domain.vo.*;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 通知公告表(SysNotice)表服务实现类
 *
 * @author makejava
 * @since 2026-05-25 20:30:59
 */
@Service("sysNoticeService")
@Slf4j
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

        //管理员优先显示未发布和已撤回通知，其他状态仍按发布时间倒序
        if (isAdmin) {
            queryWrapper.last("ORDER BY CASE WHEN status IN ('0','2') THEN 0 ELSE 1 END, publish_time DESC");
        } else {
            queryWrapper.orderByDesc(SysNotice::getPublishTime);
        }

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

    //更新通知
    @Override
    @Transactional
    public ResponseResult updateNotice(Long id, UpdateNoticeDto updateNoticeDto)
    {
        //检查通知是否存在
        SysNotice oldNotice = sysNoticeService.getById(id);
        if (oldNotice == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该通知不存在！");
        }

        //处理通知内容中的图片
        String content = updateNoticeDto.getContent();
        List<String> oldContentImages = new ArrayList<>();
        List<String> newContentImages = new ArrayList<>();

        //提取旧内容中的图片URL
        if (StringUtils.hasText(oldNotice.getContent())) {
            oldContentImages = ImageUrlUtils.extractImageUrls(oldNotice.getContent());
        }

        //处理新内容中的临时图片
        if (ImageUrlUtils.containsTempImages(content)) {
            content = processContentImagesAndSaveRecords(content, updateNoticeDto.getTitle());
            updateNoticeDto.setContent(content);
        }

        //提取新内容中的图片URL
        if (StringUtils.hasText(content)) {
            newContentImages = ImageUrlUtils.extractImageUrls(content);
        }

        //先更新通知，避免先删 OSS 再更新数据库
        SysNotice notice = BeanCopyUtils.copyBean(updateNoticeDto, SysNotice.class);
        notice.setId(id);
        boolean updated = sysNoticeService.updateById(notice);
        if (!updated) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "更新通知失败");
        }

        //如果标题发生变化，同步更新文件表里的文件来源
        if (StringUtils.hasText(oldNotice.getTitle()) && StringUtils.hasText(updateNoticeDto.getTitle())
                && !oldNotice.getTitle().equals(updateNoticeDto.getTitle())) {
            syncFileSourceByTitle(oldNotice.getTitle(), updateNoticeDto.getTitle());
        }

        //数据库更新成功后，再删除不再使用的内容图片
        for (String oldImageUrl : oldContentImages) {
            if (!newContentImages.contains(oldImageUrl)) {
                ossFileService.deleteFileByUrl(oldImageUrl);
                deleteFileRecordByUrl(oldImageUrl);
                log.info("删除旧的内容图片并同步文件表: {}", oldImageUrl);
            }
        }

        return ResponseResult.okResult();
    }

    //删除通知（批量）
    @Override
    public ResponseResult deleteNotice(Long[] ids)
    {
        //参数校验
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的通知");
        }

        List<Long> idList = List.of(ids);

        //检查通知是否存在
        List<SysNotice> noticesToDelete = sysNoticeService.listByIds(idList);
        if (noticesToDelete.size() != ids.length) {
            //找出不存在的 通知id
            Set<Long> existIds = noticesToDelete.stream()
                    .map(SysNotice::getId)
                    .collect(Collectors.toSet());

            List<Long> notExistIds = idList.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败！！原因：以下通知 id 不存在：" + notExistIds);
        }

        //遍历每个通知ID，将相关图片移动到deleted文件夹，并同步删除文件表记录
        for (Long noticeId : idList)
        {
            SysNotice notice = sysNoticeService.getById(noticeId);

            //查找需要移动的图片URL并去重
            Set<String> imageUrlSet = new LinkedHashSet<>();
            if (StringUtils.hasText(notice.getContent())) {
                List<String> contentImages = ImageUrlUtils.extractImageUrls(notice.getContent());
                imageUrlSet.addAll(contentImages);
                log.info("通知ID: {} 内容中提取到的图片URL: {}", noticeId, contentImages);
            }

            List<String> imageUrls = new ArrayList<>(imageUrlSet);
            log.info("通知ID: {} 去重后的图片URL: {}", noticeId, imageUrls);

            //批量移动图片到 deleted 文件夹
            if (!imageUrls.isEmpty()) {
                int movedCount = ossFileService.batchMoveFilesToDeleted(imageUrls);
                log.info("通知ID: {} 的图片已移动到deleted文件夹，成功: {}/{}",
                        noticeId, movedCount, imageUrls.size());

                //同步删除文件表记录
                for (String imageUrl : imageUrls) {
                    deleteFileRecordByUrl(imageUrl);
                }
                log.info("文章ID: {} 的图片文件表记录已删除，共 {} 条", noticeId, imageUrls.size());
            }
        }

        //逻辑删除通知（将 del_flag 从 0 > 1）
        sysNoticeService.removeByIds(idList);
        log.info("批量逻辑删除通知成功，ID列表: {}", idList);

        return ResponseResult.okResult();
    }

    //发布通知
    @Override
    public ResponseResult publishNotice(Long id)
    {
        //检查通知是否存在
        SysNotice oldNotice = sysNoticeService.getById(id);
        if (oldNotice == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该通知不存在！");
        }

        //已发布的通知无需重复发布
        if (SystemConstants.NOTICE_STATUS_PUBLISHED.equals(oldNotice.getStatus())) {
            return ResponseResult.okResult();
        }

        //更新通知状态并写入发布时间
        SysNotice notice = new SysNotice();
        notice.setId(id);
        notice.setStatus(SystemConstants.NOTICE_STATUS_PUBLISHED);
        notice.setPublishTime(new Date());

        boolean updated = sysNoticeService.updateById(notice);
        if (!updated) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "发布通知失败");
        }

        return ResponseResult.okResult();
    }

    //撤回通知
    @Override
    public ResponseResult revokeNotice(Long id)
    {
        //检查通知是否存在
        SysNotice oldNotice = sysNoticeService.getById(id);
        if (oldNotice == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该通知不存在！");
        }

        //已撤回的通知无需重复撤回
        if (SystemConstants.NOTICE_STATUS_REVOKED.equals(oldNotice.getStatus())) {
            return ResponseResult.okResult();
        }

        //更新通知状态并写入撤回时间
        SysNotice notice = new SysNotice();
        notice.setId(id);
        notice.setStatus(SystemConstants.NOTICE_STATUS_REVOKED);
        notice.setRevokeTime(new Date());

        boolean updated = sysNoticeService.updateById(notice);
        if (!updated) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "撤回通知失败");
        }

        return ResponseResult.okResult();
    }

    //获取通知信息详情
    @Override
    public ResponseResult<NoticeDetailVo> getNoticeDetail(Long id)
    {
        //参数校验
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "通知id不能为空");
        }

        //根据通知id 查询通知详情
        SysNotice notice = sysNoticeService.getById(id);
        if (notice == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "该通知不存在！");
        }

        //转换成Vo
        NoticeDetailVo noticeDetailVo = BeanCopyUtils.copyBean(notice, NoticeDetailVo.class);

        //回填发布人名称
        if (notice.getPublisherId() != null) {
            SysUser publisher = sysUserService.getById(notice.getPublisherId());
            if (publisher != null) {
                noticeDetailVo.setPublisherName(StringUtils.hasText(publisher.getNickname()) ? publisher.getNickname() : publisher.getUsername());
            }
        }

        //查看详情时同步阅读状态
        Long userId = SecurityUtils.getUserId();
        if (userId != null) {
            SysNoticeUser noticeUser = sysNoticeUserService.getOne(new LambdaQueryWrapper<SysNoticeUser>()
                    .eq(SysNoticeUser::getUserId, userId)
                    .eq(SysNoticeUser::getNoticeId, notice.getId())
                    .last("limit 1"));

            Date now = new Date();
            if (noticeUser == null) {
                noticeUser = new SysNoticeUser();
                noticeUser.setNoticeId(notice.getId());
                noticeUser.setUserId(userId);
                noticeUser.setIsRead(SystemConstants.NOTICE_IS_READ);
                noticeUser.setReadTime(now);
                sysNoticeUserService.save(noticeUser);
            } else if (!SystemConstants.NOTICE_IS_READ.equals(noticeUser.getIsRead())) {
                noticeUser.setIsRead(SystemConstants.NOTICE_IS_READ);
                noticeUser.setReadTime(now);
                sysNoticeUserService.updateById(noticeUser);
            }
        }

        //返回
        return ResponseResult.okResult(noticeDetailVo);
    }

    //通知全部已读
    @Override
    @Transactional
    public ResponseResult readAllNotice()
    {
        //先校验登录状态，如果是未登录返回需要登录
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //查询当前用户可见的已发布的通知
        boolean isAdmin = SecurityUtils.isAdmin();
        LambdaQueryWrapper<SysNotice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysNotice::getStatus, SystemConstants.NOTICE_STATUS_PUBLISHED);

        //将通知在 sys_notice_user 中批量标记为已读
        if (!isAdmin) {
            queryWrapper.and(wrapper -> wrapper.eq(SysNotice::getTargetType, SystemConstants.TARGET_TYPE_IS_ALL)
                    .or()
                    .apply("concat(',', target_user_ids, ',') like {0}", "%," + userId + ",%"));
        }

        //没有通知时直接返回
        List<SysNotice> noticeList = sysNoticeService.list(queryWrapper);
        if (noticeList.isEmpty()) {
            return ResponseResult.okResult();
        }

        //提取当前页可见通知ID
        List<Long> noticeIds = noticeList.stream().map(SysNotice::getId).toList();

        //查询当前用户针对这些通知是否已经存在阅读记录
        List<SysNoticeUser> noticeUserList = sysNoticeUserService.list(new LambdaQueryWrapper<SysNoticeUser>()
                .eq(SysNoticeUser::getUserId, userId)
                .in(SysNoticeUser::getNoticeId, noticeIds));

        //把已存在的阅读记录转成Map方便后续操作
        Map<Long, SysNoticeUser> noticeUserMap = noticeUserList.stream()
                .collect(Collectors.toMap(SysNoticeUser::getNoticeId, item -> item, (oldVal, newVal) -> oldVal));

        //新增未读记录和更新已有未读记录分开来收集然后再批量入库
        List<SysNoticeUser> saveList = new ArrayList<>();
        List<SysNoticeUser> updateList = new ArrayList<>();
        Date now = new Date();
        for (SysNotice notice : noticeList) {
            SysNoticeUser noticeUser = noticeUserMap.get(notice.getId());
            if (noticeUser == null) {
                //当前用户没有该通知的阅读记录，则新增一条已读记录
                SysNoticeUser newNoticeUser = new SysNoticeUser();
                newNoticeUser.setNoticeId(notice.getId());
                newNoticeUser.setUserId(userId);
                newNoticeUser.setIsRead(SystemConstants.NOTICE_IS_READ);
                newNoticeUser.setReadTime(now);
                saveList.add(newNoticeUser);
            } else if (!SystemConstants.NOTICE_IS_READ.equals(noticeUser.getIsRead())) {
                //已有记录但状态不是已读，则更新为已读（防御性编程）
                noticeUser.setIsRead(SystemConstants.NOTICE_IS_READ);
                noticeUser.setReadTime(now);
                updateList.add(noticeUser);
            }
        }

        //批量保存新增的阅读记录
        if (!saveList.isEmpty()) {
            sysNoticeUserService.saveBatch(saveList);
        }

        //批量更新已存在的阅读记录
        if (!updateList.isEmpty()) {
            sysNoticeUserService.updateBatchById(updateList);
        }

        return ResponseResult.okResult();
    }

    //获取我的通知列表
    @Override
    public ResponseResult<PageVo> getMyNoticeList(Integer pageNum, Integer pageSize, NoticeListDto noticeListDto)
    {
        //先校验登录状态，未登录无法查看我的通知
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        //构建查询条件
        LambdaQueryWrapper<SysNotice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysNotice::getStatus, SystemConstants.NOTICE_STATUS_PUBLISHED)
                .eq(SysNotice::getDelFlag, SystemConstants.STATUS_NORMAL)
                .and(wrapper -> wrapper.eq(SysNotice::getTargetType, SystemConstants.TARGET_TYPE_IS_ALL)
                        .or()
                        .apply("find_in_set({0}, target_user_ids)", userId));

        //标题模糊搜索
        if (noticeListDto != null && StringUtils.hasText(noticeListDto.getTitle())) {
            queryWrapper.like(SysNotice::getTitle, noticeListDto.getTitle());
        }

        //按发布时间倒序
        queryWrapper.orderByDesc(SysNotice::getPublishTime);

        //分页查询
        Page<SysNotice> page = new Page<>(pageNum, pageSize);
        this.page(page, queryWrapper);

        List<SysNotice> noticeList = page.getRecords();
        List<MyNoticeListVo> myNoticeListVos = BeanCopyUtils.copyBeanList(noticeList, MyNoticeListVo.class);

        //回填发布人名称
        List<Long> publisherIds = noticeList.stream()
                .map(SysNotice::getPublisherId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> publisherNameMap = new HashMap<>();
        if (!publisherIds.isEmpty()) {
            List<SysUser> publisherList = sysUserService.listByIds(publisherIds);
            publisherNameMap = publisherList.stream()
                    .collect(Collectors.toMap(SysUser::getId,
                            user -> StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername(),
                            (oldVal, newVal) -> oldVal));
        }

        for (int i = 0; i < noticeList.size(); i++) {
            SysNotice notice = noticeList.get(i);
            MyNoticeListVo myNoticeListVo = myNoticeListVos.get(i);
            myNoticeListVo.setPublisherName(publisherNameMap.get(notice.getPublisherId()));
        }

        //默认都设置为未读
        for (MyNoticeListVo myNoticeListVo : myNoticeListVos) {
            myNoticeListVo.setIsRead(SystemConstants.NOTICE_NOT_READ);
        }

        //查询并回填阅读状态
        if (!noticeList.isEmpty()) {
            List<Long> noticeIds = noticeList.stream().map(SysNotice::getId).collect(Collectors.toList());
            List<SysNoticeUser> noticeUserList = sysNoticeUserService.list(new LambdaQueryWrapper<SysNoticeUser>()
                    .eq(SysNoticeUser::getUserId, userId)
                    .in(SysNoticeUser::getNoticeId, noticeIds));

            Map<Long, String> noticeReadMap = noticeUserList.stream()
                    .collect(Collectors.toMap(SysNoticeUser::getNoticeId, SysNoticeUser::getIsRead, (oldVal, newVal) -> oldVal));

            for (MyNoticeListVo myNoticeListVo : myNoticeListVos) {
                myNoticeListVo.setIsRead(noticeReadMap.getOrDefault(myNoticeListVo.getId(), SystemConstants.NOTICE_NOT_READ));
            }
        }

        //未读优先，已读在后；同状态下按发布时间倒序
        myNoticeListVos.sort(Comparator.comparing((MyNoticeListVo vo) -> SystemConstants.NOTICE_IS_READ.equals(vo.getIsRead()))
                .thenComparing(MyNoticeListVo::getPublishTime, Comparator.nullsLast(Comparator.reverseOrder())));

        PageVo pageVo = new PageVo(myNoticeListVos, page.getTotal());
        return ResponseResult.okResult(pageVo);
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

    //处理通知内容中的图片并将其写入文件表
    private String processContentImagesAndSaveRecords(String content, String postTitle)
    {
        List<String> tempImageUrls = ImageUrlUtils.extractTempImageUrls(content);
        String result = content;

        //因为同一篇通知里可能重复引用同一张临时图片，所一这里先去重，避免重复转正和重复删除临时文件
        List<String> distinctTempImageUrls = tempImageUrls.stream().distinct().toList();

        for (String tempUrl : distinctTempImageUrls) {
            UploadFileMetaVo uploadFileMetaVo = ossFileService.moveTempToFormal(tempUrl);

            //判断图片是否保存（从oss中的 临时 > 正式）成功
            if (uploadFileMetaVo == null || !StringUtils.hasText(uploadFileMetaVo.getUrl())) {
                throw new RuntimeException("图片保存失败，无法保存通知内容中的图片记录: " + tempUrl);
            }

            result = ImageUrlUtils.replaceImageUrl(result, tempUrl, uploadFileMetaVo.getUrl());
            if (StringUtils.hasText(uploadFileMetaVo.getFilePath())) {
                String fileSource = "通知《" + postTitle + "》中的内容图片";
                saveFileRecord(uploadFileMetaVo.getName(),
                        uploadFileMetaVo.getFilePath(),
                        uploadFileMetaVo.getUrl(),
                        uploadFileMetaVo.getSize(),
                        uploadFileMetaVo.getMimeType(),
                        fileSource);
            }
        }

        return result;
    }

    //处理删除文件
    private void deleteFileRecordByUrl(String url)
    {
        if (!StringUtils.hasText(url)) {
            return;
        }

        LambdaQueryWrapper<SysFile> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysFile::getUrl, url);
        sysFileService.remove(queryWrapper);
    }

    //标题变更时同步更新文件来源
    private void syncFileSourceByTitle(String oldTitle, String newTitle)
    {
        String oldPostPrefix = "通知《" + oldTitle + "》";
        String newPostPrefix = "通知《" + newTitle + "》";

        List<SysFile> fileList = sysFileService.list(new LambdaQueryWrapper<SysFile>()
                .like(SysFile::getFileSource, oldPostPrefix));

        if (fileList.isEmpty()) {
            return;
        }

        for (SysFile sysFile : fileList) {
            String fileSource = sysFile.getFileSource();
            if (StringUtils.hasText(fileSource)) {
                sysFile.setFileSource(fileSource.replace(oldPostPrefix, newPostPrefix));
            }
        }

        sysFileService.updateBatchById(fileList);
    }

}

