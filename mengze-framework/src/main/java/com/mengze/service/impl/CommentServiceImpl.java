package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.User;
import com.mengze.domain.vo.CommentVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.CommentMapper;
import com.mengze.mapper.UserMapper;
import com.mengze.domain.entity.Comment;
import com.mengze.service.CommentService;
import com.mengze.utils.BeanCopyUtils;
import com.mengze.utils.GravatarUtils;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * 评论表(Comment)表服务实现类
 */
@Service("commentService")
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService
{
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailServiceImpl emailService;

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
    public ResponseResult addComment(Comment comment)
    {
        String type = comment.getType();
        
        //校验评论内容
        if (!StringUtils.hasText(comment.getContent())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.CONTENT_NOT_NULL);
        }

        comment.setType(type);
        comment.setArticleId(comment.getArticleId());
        comment.setContent(comment.getContent());
        comment.setRootId(comment.getRootId() != null ? comment.getRootId() : -1L);
        comment.setReplyToCommentId(comment.getReplyToCommentId());
        comment.setReplyToUserId(comment.getReplyToUserId());
        
        //根据评论类型进行权限校验
        if (SystemConstants.COMMENT_TYPE_ARTICLE.equals(type) || SystemConstants.COMMENT_TYPE_LINK.equals(type))
        {
            //在文章评论和友链评论需要登录
            if (!SecurityUtils.isLogin()) {
                return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
            }
            LoginUser loginUser = SecurityUtils.getLoginUser();
            User user = loginUser.getUser();
            comment.setUserId(user.getId());
            comment.setNickname(user.getNickname());
            comment.setEmail(user.getEmail());
            comment.setPersonalWebsite(comment.getPersonalWebsite());//添加个人网址
        } else if (SystemConstants.COMMENT_TYPE_MESSAGE.equals(type)) {
            //在留言板评论则不需要登录，但需要填写昵称和邮箱
            if (SecurityUtils.isLogin()) {
                //已登录用户使用登录信息
                LoginUser loginUser = SecurityUtils.getLoginUser();
                User user = loginUser.getUser();
                comment.setUserId(user.getId());
                comment.setNickname(user.getNickname());
                comment.setEmail(user.getEmail());
                comment.setPersonalWebsite(comment.getPersonalWebsite());
            } else {
                //未登录用户需要填写昵称和邮箱
                if (!StringUtils.hasText(comment.getNickname())) {
                    return ResponseResult.errorResult(AppHttpCodeEnum.NICKNAME_NOT_NULL);
                }
                if (!StringUtils.hasText(comment.getEmail())) {
                    return ResponseResult.errorResult(AppHttpCodeEnum.EMAIL_NOT_NULL);
                }
                comment.setNickname(comment.getNickname());
                comment.setEmail(comment.getEmail());
                comment.setPersonalWebsite(comment.getPersonalWebsite());
            }
        }

        //如果前端没有传头像，则根据用户信息获取
        if (!StringUtils.hasText(comment.getAvatar())) {
            if (comment.getUserId() != null) {
                //已登录用户，使用用户表中的最新头像
                User user = userMapper.selectById(comment.getUserId());
                if (user != null && StringUtils.hasText(user.getAvatar())) {
                    comment.setAvatar(user.getAvatar());
                } else {
                    //用户表中没有头像，根据邮箱生成（使用评论专用头像）
                    comment.setAvatar(GravatarUtils.getCommentPreviewAvatarUrl(comment.getEmail()));
                }
            } else {
                //未登录用户，根据邮箱生成（使用评论专用头像）
                comment.setAvatar(GravatarUtils.getCommentPreviewAvatarUrl(comment.getEmail()));
            }
        }
        
        //设置默认值
        comment.setLikeCount(0);
        comment.setStatus(SystemConstants.COMMENT_STATUS_NORMAL);
        comment.setDelFlag("0");
        comment.setCreateTime(new Date());

        save(comment);
        
        //发送邮件通知
        emailService.sendCommentNotificationByEmail(comment);
        
        //封装成Vo返回，判断是否为博主（通过邮箱匹配）
        CommentVo commentVo = BeanCopyUtils.copyBean(comment, CommentVo.class);
        if (StringUtils.hasText(comment.getEmail()) 
                && comment.getEmail().equalsIgnoreCase(SystemConstants.BLOGGER_EMAIL)) {
            commentVo.setUserType("1"); //博主标识
        } else if (comment.getUserId() != null) {
            //已登录用户，查询用户类型
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                commentVo.setUserType(user.getUserType());
            }
        } else {
            commentVo.setUserType("0"); //普通用户
        }

        return ResponseResult.okResult();
    }

    //点赞评论
    @Override
    public ResponseResult likeComment(Long commentId)
    {
        //查询评论是否存在且状态正常
        Comment comment = getById(commentId);
        if (comment == null || !SystemConstants.COMMENT_STATUS_NORMAL.equals(comment.getStatus())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.COMMENT_NOT_EXIST);
        }
        
        // 点赞数+1
        comment.setLikeCount(comment.getLikeCount() + 1);
        updateById(comment);
        
        // 返回更新后的点赞数
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

    //转换为CommentVo列表，并设置回复目标昵称和用户类型
    private List<CommentVo> toCommentVoList(List<Comment> comments)
    {
        List<CommentVo> voList = BeanCopyUtils.copyBeanList(comments, CommentVo.class);
        for (int i = 0; i < voList.size(); i++) {
            CommentVo vo = voList.get(i);
            Comment original = comments.get(i);
            
            // 只有评论时已登录（有user_id）的评论才实时获取最新头像和昵称
            if (original.getUserId() != null) {
                User user = userMapper.selectById(original.getUserId());
                if (user != null) {
                    // 使用用户表中的最新头像和昵称
                    vo.setAvatar(user.getAvatar());
                    vo.setNickname(user.getNickname());
                    vo.setUserType(user.getUserType());
                } else {
                    // 用户不存在，使用评论表中的数据
                    vo.setAvatar(original.getAvatar());
                    vo.setUserType("0");
                }
            } else {
                // 未登录时的评论，保持匿名性，使用评论表中保存的头像
                vo.setAvatar(original.getAvatar());
                // 优先通过邮箱判断是否为博主（支持匿名评论识别博主）
                if (StringUtils.hasText(original.getEmail()) 
                        && original.getEmail().equalsIgnoreCase(SystemConstants.BLOGGER_EMAIL)) {
                    vo.setUserType("1"); //博主标识
                } else {
                    vo.setUserType("0"); //普通用户
                }
            }
            
            // 设置回复目标昵称和用户类型
            if (vo.getReplyToCommentId() != null && vo.getReplyToCommentId() > 0) {
                Comment replyTo = getById(vo.getReplyToCommentId());
                if (replyTo != null) {
                    // 如果被回复的评论是登录后发布的，获取最新昵称
                    if (replyTo.getUserId() != null) {
                        User replyToUser = userMapper.selectById(replyTo.getUserId());
                        if (replyToUser != null) {
                            vo.setReplyToCommentNickname(replyToUser.getNickname());
                            vo.setReplyToCommentUserType(replyToUser.getUserType());
                        } else {
                            vo.setReplyToCommentNickname(replyTo.getNickname());
                            vo.setReplyToCommentUserType("0");
                        }
                    } else {
                        // 被回复的评论是匿名评论，使用评论表中的昵称
                        vo.setReplyToCommentNickname(replyTo.getNickname());
                        // 判断被回复者是否为博主
                        if (StringUtils.hasText(replyTo.getEmail()) 
                                && replyTo.getEmail().equalsIgnoreCase(SystemConstants.BLOGGER_EMAIL)) {
                            vo.setReplyToCommentUserType("1"); //博主或者管理员
                        } else {
                            vo.setReplyToCommentUserType("0"); //普通用户
                        }
                    }
                }
            }
        }
        return voList;
    }
}
