package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.CommentListDto;
import com.mengze.domain.dto.ReplyCommentDto;
import com.mengze.domain.entity.Article;
import com.mengze.domain.entity.Comment;
import com.mengze.domain.entity.LoginUser;
import com.mengze.domain.entity.SysUser;
import com.mengze.domain.vo.AdminCommentListVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.ArticleMapper;
import com.mengze.mapper.CommentMapper;
import com.mengze.service.AdminCommentService;
import com.mengze.service.CommentService;
import com.mengze.utils.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 评论管理服务实现类
 *
 * @author mengze
 */
@Service
public class AdminCommentServiceImpl implements AdminCommentService
{
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private CommentServiceImpl commentService;

    //获取评论列表
    @Override
    public ResponseResult getCommentList(Integer pageNum, Integer pageSize, CommentListDto commentListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        
        //关键字模糊搜索评论内容
        queryWrapper.like(StringUtils.hasText(commentListDto.getKeywords()), Comment::getContent, commentListDto.getKeywords());
        
        //评论类型筛选
        queryWrapper.eq(StringUtils.hasText(commentListDto.getType()), Comment::getType, commentListDto.getType());
        
        //审核状态筛选
        queryWrapper.eq(StringUtils.hasText(commentListDto.getStatus()), Comment::getStatus, commentListDto.getStatus());
        
        //文章ID筛选(type=0，即为文章评论时)
        if (StringUtils.hasText(commentListDto.getArticleId()) 
                && SystemConstants.COMMENT_TYPE_ARTICLE.equals(commentListDto.getType())) {
            queryWrapper.eq(Comment::getArticleId, Long.parseLong(commentListDto.getArticleId()));
        }
        
        //时间范围筛选
        queryWrapper.ge(StringUtils.hasText(commentListDto.getStartTime()), Comment::getCreateTime, commentListDto.getStartTime())
                    .le(StringUtils.hasText(commentListDto.getEndTime()), Comment::getCreateTime, commentListDto.getEndTime());
        
        //按创建时间倒序排列
        queryWrapper.orderByDesc(Comment::getCreateTime);
        
        //分页查询
        Page<Comment> page = new Page<>(pageNum, pageSize);
        commentMapper.selectPage(page, queryWrapper);
        
        //转换为VO并构建树形结构
        List<AdminCommentListVo> commentVoList = buildCommentTree(page.getRecords());
        
        return ResponseResult.okResult(new PageVo(commentVoList, page.getTotal()));
    }

    //回复评论（管理员回复）
    @Override
    public ResponseResult replyComment(ReplyCommentDto replyCommentDto)
    {
        //获取当前登录的管理员信息
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN, "用户未登录");
        }
        
        //判断是否是系统管理员
        if (!SecurityUtils.isAdmin()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NO_OPERATOR_AUTH, "只有系统管理员才能回复评论");
        }
        
        SysUser currentUser = loginUser.getSysUser();
        
        //创建评论对象并复制前端传递的字段
        Comment comment = new Comment();
        BeanUtils.copyProperties(replyCommentDto, comment);
        
        //补充管理员信息
        comment.setUserId(currentUser.getId());
        comment.setNickname(currentUser.getNickname());
        comment.setEmail(currentUser.getEmail());
        comment.setAvatar(currentUser.getAvatar());
        
        //设置默认值
        comment.setStatus(SystemConstants.COMMENT_STATUS_NORMAL);  // 管理员评论默认审核通过
        comment.setLikeCount(SystemConstants.DEFAULT_COMMENT_LIKES);  // 初始化点赞数
        
        //保存评论
        int result = commentMapper.insert(comment);
        
        if (result > 0) {
            return ResponseResult.okResult("回复成功");
        } else {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "回复失败");
        }
    }

    //审核评论（允许评论发布）
    @Override
    public ResponseResult approveComment(Long[] ids)
    {
        // 参数校验
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要审核的评论");
        }

        //获取评论id列表
        List<Long> idList = List.of(ids);

        //查询需要审核的评论
        List<Comment> commentsToApprove = commentService.listByIds(idList);
        if (commentsToApprove.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "评论不存在");
        }

        //检查是否有不属于待审核状态的评论
        boolean hasInvalidStatus = commentsToApprove.stream()
                .anyMatch(comment -> !SystemConstants.COMMENT_STATUS_PENDING_REVIEW.equals(comment.getStatus()));

        if (hasInvalidStatus) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR,
                    "所选评论中含有状态为'正常'或'审核未通过'评论，请重新选择!");
        }

        //批量更新评论状态为正常
        commentsToApprove.forEach(comment -> {
            comment.setStatus(SystemConstants.COMMENT_STATUS_NORMAL);
        });

        //TODO 发送审核通过邮箱给发布评论的用户

        //批量更新到数据库
        boolean updateResult = commentService.updateBatchById(commentsToApprove);

        if (updateResult) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode(),
                    "成功审核通过 " + commentsToApprove.size() + " 条评论");
        } else {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "审核失败");
        }
    }

    //审核评论（拒绝评论发布）
    @Override
    public ResponseResult rejectComment(Long[] ids)
    {
        // 参数校验
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要审核的评论");
        }

        //获取评论id列表
        List<Long> idList = List.of(ids);

        //查询需要审核的评论
        List<Comment> commentsToReject = commentService.listByIds(idList);
        if (commentsToReject.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "评论不存在");
        }

        //检查是否有不属于待审核状态的评论
        boolean hasInvalidStatus = commentsToReject.stream()
                .anyMatch(comment -> !SystemConstants.COMMENT_STATUS_PENDING_REVIEW.equals(comment.getStatus()));

        if (hasInvalidStatus) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR,
                    "所选评论中含有状态为'正常'或'审核未通过'评论，请重新选择!");
        }

        //批量更新评论状态为审核未通过
        commentsToReject.forEach(comment -> {
            comment.setStatus(SystemConstants.COMMENT_STATUS_REVIEW_NOT_PASSED);
        });

        //TODO 发送审核未通过邮箱给发布评论的用户

        //批量更新到数据库
        boolean updateResult = commentService.updateBatchById(commentsToReject);

        if (updateResult) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode(),
                    "成功拒绝 " + commentsToReject.size() + " 条评论进行发布");
        } else {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "审核失败");
        }
    }

    //删除评论（逻辑删除）
    @Override
    public ResponseResult deleteComment(Long[] ids)
    {
        // 参数校验
        if (ids == null || ids.length == 0) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "请选择要删除的评论");
        }

        //获取评论id列表
        List<Long> idList = List.of(ids);
        
        //查询要删除的评论，找出所有根评论
        List<Comment> commentsToDelete = commentMapper.selectBatchIds(idList);
        if (commentsToDelete.isEmpty()) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "评论不存在");
        }
        
        //收集所有需要删除的评论ID（包括子评论）
        List<Long> allIdsToDelete = new ArrayList<>(idList);
        
        for (Comment comment : commentsToDelete)
        {
            //如果是根评论则需要删除其所有子评论
            if (comment.getRootId() == null || comment.getRootId() == -1)
            {
                LambdaQueryWrapper<Comment> childWrapper = new LambdaQueryWrapper<>();
                childWrapper.eq(Comment::getRootId, comment.getId());
                List<Comment> childComments = commentMapper.selectList(childWrapper);
                
                //添加子评论ID到删除列表
                List<Long> childIds = childComments.stream()
                        .map(Comment::getId)
                        .collect(Collectors.toList());
                allIdsToDelete.addAll(childIds);
            }
        }
        
        //删除评论
        int deleteCount = commentMapper.deleteBatchIds(allIdsToDelete);
        
        if (deleteCount > 0) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode(), 
                    "成功删除 " + deleteCount + " 条评论");
        } else {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败");
        }
    }

    //构建评论树形结构
    private List<AdminCommentListVo> buildCommentTree(List<Comment> comments)
    {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        //先转换为VO列表
        List<AdminCommentListVo> allVoList = toAdminCommentVoList(comments);
        
        //使用Map存储所有评论，key为评论ID
        Map<String, AdminCommentListVo> commentMap = allVoList.stream()
                .collect(Collectors.toMap(AdminCommentListVo::getId, vo -> vo));
        
        //分离根评论和子评论
        List<AdminCommentListVo> rootComments = new ArrayList<>();
        
        for (AdminCommentListVo vo : allVoList) {
            if (vo.getRootId() == null || vo.getRootId() == -1) {
                //根评论
                rootComments.add(vo);
            } else {
                //子评论，找到父评论并其添加到children中
                String rootIdStr = String.valueOf(vo.getRootId());
                AdminCommentListVo rootComment = commentMap.get(rootIdStr);
                if (rootComment != null) {
                    rootComment.getChildren().add(vo);
                } else {
                    //如果找不到根评论（可能不在当前页），则作为根评论处理
                    rootComments.add(vo);
                }
            }
        }
        
        return rootComments;
    }

    //转换为AdminCommentVo列表
    private List<AdminCommentListVo> toAdminCommentVoList(List<Comment> comments)
    {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        //批量查询文章标题（避免N+1查询）
        List<Long> articleIds = comments.stream()
                .filter(c -> c.getArticleId() != null && c.getArticleId() > 0)
                .map(Comment::getArticleId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, String> articleTitleMap = new HashMap<>();
        if (!articleIds.isEmpty()) {
            List<Article> articles = articleMapper.selectBatchIds(articleIds);
            articleTitleMap = articles.stream()
                    .collect(Collectors.toMap(Article::getId, Article::getTitle));
        }
        
        //批量查询回复目标昵称
        List<Long> replyToCommentIds = comments.stream()
                .filter(c -> c.getReplyToCommentId() != null && c.getReplyToCommentId() > 0)
                .map(Comment::getReplyToCommentId)
                .distinct()
                .collect(Collectors.toList());
        
        Map<Long, String> replyToNicknameMap = new HashMap<>();
        if (!replyToCommentIds.isEmpty()) {
            List<Comment> replyToComments = commentMapper.selectBatchIds(replyToCommentIds);
            replyToNicknameMap = replyToComments.stream()
                    .collect(Collectors.toMap(Comment::getId, Comment::getNickname));
        }
        
        //转换为VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<AdminCommentListVo> voList = new ArrayList<>();
        
        for (Comment comment : comments)
        {
            AdminCommentListVo vo = new AdminCommentListVo();
            vo.setId(String.valueOf(comment.getId()));
            
            //直接使用数字类型
            vo.setType(comment.getType());
            
            // 设置文章信息
            if (comment.getArticleId() != null && comment.getArticleId() > 0) {
                vo.setArticleId(String.valueOf(comment.getArticleId()));
                vo.setArticleTitle(articleTitleMap.get(comment.getArticleId()));
            }
            
            //设置用户信息
            if (comment.getUserId() != null) {
                vo.setUserId(String.valueOf(comment.getUserId()));
            }
            vo.setNickname(comment.getNickname());
            vo.setEmail(comment.getEmail());
            vo.setPersonalWebsite(comment.getPersonalWebsite());
            
            //设置评论内容
            vo.setContent(comment.getContent());
            vo.setRootId(comment.getRootId());
            
            //设置回复目标昵称
            if (comment.getReplyToCommentId() != null && comment.getReplyToCommentId() > 0) {
                vo.setReplyToNickname(replyToNicknameMap.get(comment.getReplyToCommentId()));
            }
            
            vo.setLikeCount(comment.getLikeCount());
            vo.setStatus(comment.getStatus());
            vo.setCreateTime(sdf.format(comment.getCreateTime()));
            
            //子评论列表初始化为空
            vo.setChildren(new ArrayList<>());
            
            voList.add(vo);
        }
        
        return voList;
    }
}
