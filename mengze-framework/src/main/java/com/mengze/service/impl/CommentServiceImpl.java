package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.AddCommentDto;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.SysUser;
import com.mengze.domain.vo.CommentVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.CommentMapper;
import com.mengze.mapper.SysUserMapper;
import com.mengze.mapper.SysRoleMapper;
import com.mengze.domain.entity.Comment;
import com.mengze.domain.entity.Article;
import com.mengze.service.CommentService;
import com.mengze.service.ArticleService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.GravatarUtils;
import com.mengze.utils.SecurityUtils;
import com.mengze.utils.XssUtils;
import com.mengze.service.SensitiveUtilService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 评论表(Comment)表服务实现类
 */
@Service("commentService")
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService
{
    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private EmailServiceImpl emailService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private SensitiveUtilService sensitiveUtilService;

    //查询评论列表（包括文章评论，友链评论和留言板评论）
    @Override
    public ResponseResult commentList(Integer type, Long articleId, Integer pageNum, Integer pageSize)
    {
        // 查询根评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getType, String.valueOf(type));
        // 文章评论需要关联articleId
        if (SystemConstants.COMMENT_TYPE_ARTICLE.equals(String.valueOf(type))) {
            queryWrapper.eq(Comment::getArticleId, articleId);
        }
        queryWrapper.eq(Comment::getRootId, SystemConstants.COMMENT_ROOT_ID);
        queryWrapper.eq(Comment::getStatus, SystemConstants.COMMENT_STATUS_NORMAL);
        queryWrapper.orderByDesc(Comment::getCreateTime);

        //分页查询
        Page<Comment> page = new Page<>(pageNum, pageSize);
        page(page, queryWrapper);

        //转换为VO并设置Gravatar头像
        List<CommentVo> commentVoList = toCommentVoList(page.getRecords());
        
        //查询子评论
        for (CommentVo commentVo : commentVoList) {
            List<CommentVo> children = getChildren(commentVo.getId());
            commentVo.setChildren(children);
        }

        return ResponseResult.okResult(new PageVo(commentVoList, page.getTotal()));
    }

    //添加评论
    @Override
    @Transactional
    public ResponseResult addComment(AddCommentDto addCommentDto)
    {
        //校验评论内容
        if (!StringUtils.hasText(addCommentDto.getContent())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.CONTENT_NOT_NULL);
        }

        String type = addCommentDto.getType();
        
        //如果是文章评论，需要校验文章是否允许评论
        if (SystemConstants.COMMENT_TYPE_ARTICLE.equals(type)) {
            Long articleId = addCommentDto.getArticleId();
            if (articleId == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), "文章ID不能为空");
            }
            
            //查询文章信息
            Article article = articleService.getById(articleId);
            if (article == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), "文章不存在");
            }
            
            //检查文章是否允许评论
            if (!SystemConstants.ALLOW_COMMENT.equals(article.getIsComment())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR.getCode(), "该文章不允许评论");
            }
        }

        Comment comment = BeanCopyUtils.copyBean(addCommentDto, Comment.class);
        
        //XSS防护：转义用户输入的内容
        if (StringUtils.hasText(comment.getContent())) {
            comment.setContent(XssUtils.escapeHtml(comment.getContent()));
        }
        if (StringUtils.hasText(comment.getNickname())) {
            comment.setNickname(XssUtils.escapeHtml(comment.getNickname()));
        }
        if (StringUtils.hasText(comment.getPersonalWebsite())) {
            // 验证URL安全性
            if (!XssUtils.isSafeUrl(comment.getPersonalWebsite())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "个人网址包含不安全的协议");
            }
        }
        
        //处理 rootId 默认值
        if (comment.getRootId() == null) {
            comment.setRootId(-1L);
        }
        
        //根据评论类型进行权限校验和用户信息设置
        if (SystemConstants.COMMENT_TYPE_ARTICLE.equals(type) || SystemConstants.COMMENT_TYPE_LINK.equals(type))
        {
            //文章评论和友链评论需要登录
            if (!SecurityUtils.isLogin()) {
                return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
            }
            LoginUser loginUser = SecurityUtils.getLoginUser();
            SysUser sysUser = loginUser.getSysUser();
            comment.setUserId(sysUser.getId());
            comment.setNickname(sysUser.getNickname());
            // 优先使用登录用户邮箱，若登录用户未绑定邮箱则回退使用前端传入的邮箱
            String email = sysUser.getEmail();
            if (!StringUtils.hasText(email)) {
                email = addCommentDto.getEmail();
            }
            comment.setEmail(email);
        } else if (SystemConstants.COMMENT_TYPE_MESSAGE.equals(type)) {
            //留言板评论可以不登录
            if (SecurityUtils.isLogin()) {
                //已登录用户使用登录信息
                LoginUser loginUser = SecurityUtils.getLoginUser();
                SysUser sysUser = loginUser.getSysUser();
                comment.setUserId(sysUser.getId());
                comment.setNickname(sysUser.getNickname());
                // 优先使用登录用户邮箱，若登录用户未绑定邮箱则回退使用前端传入的邮箱
                String email = sysUser.getEmail();
                if (!StringUtils.hasText(email)) {
                    email = addCommentDto.getEmail();
                }
                comment.setEmail(email);
            } else {
                //未登录用户需要填写昵称和邮箱
                if (!StringUtils.hasText(addCommentDto.getNickname())) {
                    return ResponseResult.errorResult(AppHttpCodeEnum.NICKNAME_NOT_NULL);
                }
                if (!StringUtils.hasText(addCommentDto.getEmail())) {
                    return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_NOT_NULL);
                }
                
                //敏感词检测 - 昵称（未登录用户填写的昵称）
                if (sensitiveUtilService.checkSensitiveWordsSimple(addCommentDto.getNickname())) {
                    return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "昵称包含敏感词，请修改后重试");
                }
            }
        }

        //根据用户信息获取头像
        if (comment.getUserId() != null) {
            //已登录用户，使用用户表中的最新头像
            SysUser sysUser = sysUserMapper.selectById(comment.getUserId());
            if (sysUser != null && StringUtils.hasText(sysUser.getAvatar())) {
                comment.setAvatar(sysUser.getAvatar());
            } else {
                //用户表中没有头像，根据邮箱生成（使用评论专用头像）
                comment.setAvatar(GravatarUtils.getCommentPreviewAvatarUrl(comment.getEmail()));
            }
        } else {
            //未登录用户，根据邮箱生成（使用评论专用头像）
            comment.setAvatar(GravatarUtils.getCommentPreviewAvatarUrl(comment.getEmail()));
        }
        
        //敏感词检测
        boolean isSensitive = sensitiveUtilService.checkSensitiveWords(comment.getContent(), comment.getEmail());
        
        //设置默认值
        comment.setLikeCount(SystemConstants.DEFAULT_COMMENT_LIKES);
        
        //根据敏感词检测结果设置评论状态
        if (isSensitive) {
            //检测到敏感词，设置为待审核状态
            comment.setStatus(SystemConstants.COMMENT_STATUS_PENDING_REVIEW);
        } else {
            //未检测到敏感词，设置为正常状态
            comment.setStatus(SystemConstants.COMMENT_STATUS_NORMAL);
        }
        
        comment.setDelFlag(SystemConstants.COMMENT_STATUS_NORMAL);
        comment.setCreateTime(new Date());

        save(comment);
        
        //如果检测到敏感词，发送敏感词相关的邮件通知
        if (isSensitive) {
            //发送敏感词检测邮件通知（包括给用户和博主）
            emailService.sendSensitiveWordNotification(comment);
            
            //返回提示信息
            return ResponseResult.errorResult(AppHttpCodeEnum.COMMENT_SENSITIVE_PENDING_REVIEW);
        }
        
        //未检测到敏感词，发送正常的评论通知邮件
        emailService.sendCommentNotificationByEmail(comment);

        return ResponseResult.okResult();
    }

    //点赞评论
    @Override
    public ResponseResult likeComment(Long commentId)
    {
        //判断点赞评论是否为空
        if (commentId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "评论ID不能为空");
        }

        int rows = baseMapper.increaseLikeCount(commentId);
        if (rows == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.COMMENT_NOT_EXIST);
        }

        return ResponseResult.okResult();
    }

    //生成评论头像
    @Override
    public ResponseResult generateAvatar(String email)
    {
        // 使用工具类生成头像URL
        String avatarUrl = GravatarUtils.getCommentPreviewAvatarUrl(email);
        
        // 封装返回结果
        java.util.Map<String, String> result = new java.util.HashMap<>();
        result.put("avatar", avatarUrl);
        
        return ResponseResult.okResult(result);
    }

    //获取子评论列表
    private List<CommentVo> getChildren(Long rootId)
    {
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getRootId, rootId);
        queryWrapper.eq(Comment::getStatus, SystemConstants.COMMENT_STATUS_NORMAL);
        queryWrapper.orderByAsc(Comment::getCreateTime);
        List<Comment> comments = list(queryWrapper);
        return toCommentVoList(comments);
    }

    //转换为CommentVo列表，并设置回复目标昵称和用户角色
    private List<CommentVo> toCommentVoList(List<Comment> comments)
    {
        List<CommentVo> voList = BeanCopyUtils.copyBeanList(comments, CommentVo.class);
        for (int i = 0; i < voList.size(); i++) {
            CommentVo vo = voList.get(i);
            Comment original = comments.get(i);
            
            // 只有评论时已登录（有user_id）的评论才实时获取最新头像、昵称和角色
            if (original.getUserId() != null) {
                SysUser sysUser = sysUserMapper.selectById(original.getUserId());
                if (sysUser != null) {
                    // 使用用户表中的最新头像和昵称
                    vo.setAvatar(sysUser.getAvatar());
                    vo.setNickname(sysUser.getNickname());
                    // 查询用户角色
                    List<String> roles = sysRoleMapper.selectRoleCodeByUserId(sysUser.getId());
                    vo.setRoles(roles != null && !roles.isEmpty() ? roles : Collections.emptyList());
                } else {
                    // 用户不存在，使用评论表中的数据
                    vo.setAvatar(original.getAvatar());
                    vo.setRoles(Collections.emptyList());
                }
            } else {
                // 未登录时的评论,使用评论表中保存的头像
                vo.setAvatar(original.getAvatar());
                // 优先通过邮箱判断是否为博主，未登录时也可识别
                if (StringUtils.hasText(original.getEmail()) 
                        && original.getEmail().equalsIgnoreCase(SystemConstants.BLOGGER_EMAIL)) {
                    // 博主标识 - 设置为admin角色
                    vo.setRoles(Collections.singletonList(SystemConstants.ROLE_IS_ADMIN));
                } else {
                    // 普通用户 - 空角色列表
                    vo.setRoles(Collections.emptyList());
                }
            }
            
            // 设置回复目标昵称和用户角色
            if (vo.getReplyToCommentId() != null && vo.getReplyToCommentId() > 0)
            {
                Comment replyTo = getById(vo.getReplyToCommentId());
                if (replyTo != null)
                {
                    // 如果被回复的评论是登录后发布的，获取最新昵称和角色
                    if (replyTo.getUserId() != null)
                    {
                        SysUser replyToSysUser = sysUserMapper.selectById(replyTo.getUserId());
                        if (replyToSysUser != null) {
                            vo.setReplyToCommentNickname(replyToSysUser.getNickname());
                            // 查询被回复用户的角色
                            List<String> replyToRoles = sysRoleMapper.selectRoleCodeByUserId(replyToSysUser.getId());
                            vo.setReplyToCommentRoles(replyToRoles != null && !replyToRoles.isEmpty() ? replyToRoles : Collections.emptyList());
                        } else {
                            vo.setReplyToCommentNickname(replyTo.getNickname());
                            vo.setReplyToCommentRoles(Collections.emptyList());
                        }
                    } else {
                        // 被回复的评论是匿名评论，使用评论表中的昵称
                        vo.setReplyToCommentNickname(replyTo.getNickname());
                        // 判断被回复者是否为博主
                        if (StringUtils.hasText(replyTo.getEmail()) 
                                && replyTo.getEmail().equalsIgnoreCase(SystemConstants.BLOGGER_EMAIL)) {
                            // 博主或者管理员 - 设置为admin角色
                            vo.setReplyToCommentRoles(Collections.singletonList(SystemConstants.ROLE_IS_ADMIN));
                        } else {
                            // 普通用户 - 空角色列表
                            vo.setReplyToCommentRoles(Collections.singletonList(SystemConstants.ROLE_IS_VISITOR));
                        }
                    }
                }
            }
        }
        return voList;
    }
}
