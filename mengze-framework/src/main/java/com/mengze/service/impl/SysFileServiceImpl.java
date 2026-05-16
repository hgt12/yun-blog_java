package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.FileListDto;
import com.mengze.domain.entity.Article;
import com.mengze.domain.entity.SysFile;
import com.mengze.domain.entity.SysUser;
import com.mengze.domain.vo.FileListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.SysFileMapper;
import com.mengze.service.ArticleService;
import com.mengze.service.SysFileService;
import com.mengze.service.SysUserService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.ImageUrlUtils;
import com.mengze.utils.PathUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 上传文件元数据表(SysFile)表服务实现类
 *
 * @author makejava
 * @since 2026-05-07 17:57:46
 */
@Service("sysFileService")
@Slf4j
public class SysFileServiceImpl extends ServiceImpl<SysFileMapper, SysFile> implements SysFileService
{
    @Autowired
    private SysFileService sysFileService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private OssFileServiceImpl ossFileService;

    @Value("${oss.formal-path-prefix:formal/}")
    private String formalPathPrefix;

    //上传文件
    @Override
    @Transactional
    public ResponseResult uploadFile(MultipartFile file)
    {
        //判断上传的文件是否为空
        if (file == null || file.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.FILE_NULL, "文件不能为空");
        }

        //判断上传名字是否为空
        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "文件名不能为空");
        }

        //上传至oss（注意是上传到正式文件里）
        String filePath = formalPathPrefix + PathUtils.generateFilePath(originalFilename);
        String url = ossFileService.uploadOss(file, filePath);
        if (!StringUtils.hasText(url)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "文件上传失败");
        }

        saveFileRecord(originalFilename, filePath, url, file.getSize(), file.getContentType(), "后台文件上传");
        return ResponseResult.okResult(url);
    }

    //获取文件列表
    @Override
    public ResponseResult<PageVo> getFileList(Integer pageNum, Integer pageSize, FileListDto fileListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<SysFile> queryWrapper = new LambdaQueryWrapper<>();

        //根据关键字(文件名字，文件来源)模糊查询文件
        if (StringUtils.hasText(fileListDto.getKeywords())) {
            queryWrapper.and(wrapper -> wrapper
                        .like(SysFile::getName, fileListDto.getKeywords())
                        .or()
                        .like(SysFile::getFileSource, fileListDto.getKeywords())
                );
        }

        //按创建时间倒序排序
        queryWrapper.orderByDesc(SysFile::getCreateTime);

        //分页查询
        Page<SysFile> page = new Page<>(pageNum, pageSize);
        sysFileService.page(page, queryWrapper);

        //封装成vo返回
        List<FileListVo> fileListVos = BeanCopyUtils.copyBeanList(page.getRecords(), FileListVo.class);
        PageVo pageVo = new PageVo(fileListVos, page.getTotal());

        return ResponseResult.okResult(pageVo);
    }

    //删除文件（批量）
    @Override
    @Transactional
    public ResponseResult deleteFile(Long[] ids)
    {
        //检测文件是否为空
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的文件");
        }

        //获得要删除文件的id列表
        List<Long> idList = List.of(ids);
        List<SysFile> filesToDelete = listByIds(idList);

        //检查要删除的文件是否存在，如果列表中要删除的文件不纯在则删除失败
        if (filesToDelete.size() != idList.size())
        {
            Set<Long> existIds = filesToDelete.stream()
                    .map(SysFile::getId)
                    .collect(Collectors.toSet());

            //找出不存在的 文件id
            List<Long> notExistIds = idList.stream()
                    .filter(id -> !existIds.contains(id))
                    .collect(Collectors.toList());

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败，以下文件ID不存在：" + notExistIds);
        }

        //根据选中的文件URL查询相关文章
        List<String> fileUrls = filesToDelete.stream()
                .map(SysFile::getUrl)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());

        Map<String, List<Article>> fileArticleUsageMap = new HashMap<>();
        if (!fileUrls.isEmpty())
        {
            List<Article> relatedArticles = articleService.list(
                    Wrappers.<Article>lambdaQuery()
                            .select(Article::getId, Article::getTitle, Article::getThumbnail, Article::getContent)
                            .and(wrapper -> {
                                wrapper.in(Article::getThumbnail, fileUrls);
                                for (String fileUrl : fileUrls) {
                                    wrapper.or().like(Article::getContent, fileUrl);
                                }
                            })
            );

            for (Article article : relatedArticles)
            {
                //获取文章中的缩略图
                if (StringUtils.hasText(article.getThumbnail()) && fileUrls.contains(article.getThumbnail())) {
                    fileArticleUsageMap.computeIfAbsent(article.getThumbnail(), key -> new ArrayList<>())
                            .add(article);
                }

                //获取文章内容中的图片
                List<String> contentImages = ImageUrlUtils.extractImageUrls(article.getContent());
                if (!contentImages.isEmpty()) {
                    for (String imageUrl : contentImages) {
                        if (StringUtils.hasText(imageUrl) && fileUrls.contains(imageUrl)) {
                            fileArticleUsageMap.computeIfAbsent(imageUrl, key -> new ArrayList<>())
                                    .add(article);
                        }
                    }
                }
            }
        }

        //检验选中的文件中是否能被删除
        List<String> errorMsg = new ArrayList<>();
        for (SysFile file : filesToDelete)
        {
            //获取文件url
            String fileUrl = file.getUrl();

            //如果文件正在被文章（包含缩略图或内容）使用，不允许删除
            List<Article> usedArticles = fileArticleUsageMap.get(fileUrl);
            if (usedArticles != null && !usedArticles.isEmpty()) {
                String articleNames = usedArticles.stream()
                        .map(Article::getTitle)
                        .filter(StringUtils::hasText)
                        .distinct()
                        .collect(Collectors.joining("、"));
                if (!StringUtils.hasText(articleNames)) {
                    articleNames = usedArticles.stream()
                            .map(article -> String.valueOf(article.getId()))
                            .distinct()
                            .collect(Collectors.joining("、"));
                }
                errorMsg.add("【" + file.getName() + "】正在被文章【" + articleNames + "】使用");
            }

            //如果文件正在被用户头像使用，不允许删除
            SysUser user = sysUserService.getOne(
                    Wrappers.<SysUser>lambdaQuery()
                            .eq(SysUser::getAvatar, fileUrl)
                            .select(SysUser::getUsername)
            );

            if (user != null && StringUtils.hasText(user.getUsername())) {
                errorMsg.add("【" + file.getName() + "】正在被用户【" + user.getUsername() + "】使用");
            }
        }

        if (!errorMsg.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, errorMsg + "，无法删除");
        }

        boolean removed = removeByIds(idList);
        if (!removed) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "文件删除失败");
        }

        //从oss中删除（移动到delete/文件夹内）
        for (SysFile file : filesToDelete) {
            if (file != null && StringUtils.hasText(file.getUrl())) {
                ossFileService.moveFileToDeleted(file.getUrl());
            }
        }

        return ResponseResult.okResult();
    }

    //保存到文件表 方法
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

