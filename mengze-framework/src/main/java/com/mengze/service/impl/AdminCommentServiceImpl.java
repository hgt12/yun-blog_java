package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mengze.constants.SystemConstants;
import com.mengze.domain.ResponseResult;
import com.mengze.domain.dto.CommentListDto;
import com.mengze.domain.dto.ReplyCommentDto;
import com.mengze.domain.entity.*;
import com.mengze.domain.vo.AdminCommentListVo;
import com.mengze.domain.vo.CommentExportVo;
import com.mengze.domain.vo.CommentStatisticsVo;
import com.mengze.domain.vo.PageVo;
import com.mengze.enums.AppHttpCodeEnum;
import com.mengze.mapper.ArticleMapper;
import com.mengze.mapper.CommentMapper;
import com.mengze.service.AdminCommentService;
import com.mengze.service.CommentService;
import com.mengze.utils.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论管理服务实现类
 *
 * @author mengze
 */
@Service
public class AdminCommentServiceImpl implements AdminCommentService
{
    private static final Logger log = LoggerFactory.getLogger(AdminCommentServiceImpl.class);

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CommentService commentService;

    @Autowired
    private EmailServiceImpl emailService;

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

        //获取评论id
        Long commentId = replyCommentDto.getParentId();
        if (commentId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN, "评论不存在");
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
    @Transactional(rollbackFor = Exception.class)
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
        if (commentsToApprove.size() != ids.length) {
            //找出不存在的 评论id
            Set<Long> existIds = commentsToApprove.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toSet());

            List<Long> notExistIds = idList.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "审核失败！！原因：以下评论 id 不存在：" + notExistIds);
        }

        //检查是否有不属于待审核状态的评论
        boolean hasInvalidStatus = commentsToApprove.stream()
                .anyMatch(comment -> !SystemConstants.COMMENT_STATUS_PENDING_REVIEW.equals(comment.getStatus()));

        if (hasInvalidStatus) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR,
                    "所选评论中含有状态为'正常'或'审核未通过'评论，请重新选择!");
        }

        //批量更新评论状态为正常
        commentsToApprove.forEach(comment -> comment.setStatus(SystemConstants.COMMENT_STATUS_NORMAL));

        //先更新数据库
        boolean updateResult = commentService.updateBatchById(commentsToApprove);
        if (!updateResult) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "审核失败");
        }

        //数据库更新成功后再发送审核通过邮件
        commentsToApprove.forEach(comment -> {
            if (!StringUtils.hasText(comment.getEmail())) {
                return;
            }

            try {
                emailService.sendCommentApprovedNotification(comment);
            } catch (Exception e) {
                log.error("发送评论审核通过邮件失败，commentId={}, email={}", comment.getId(), comment.getEmail(), e);
            }
        });

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode(),
                "成功审核通过 " + commentsToApprove.size() + " 条评论");
    }

    //审核评论（拒绝评论发布）
    @Override
    @Transactional(rollbackFor = Exception.class)
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
        if (commentsToReject.size() != ids.length) {
            //找出不存在的 评论id
            Set<Long> existIds = commentsToReject.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toSet());

            List<Long> notExistIds = idList.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "审核失败！！原因：以下评论 id 不存在：" + notExistIds);
        }

        //检查是否有不属于待审核状态的评论
        boolean hasInvalidStatus = commentsToReject.stream()
                .anyMatch(comment -> !SystemConstants.COMMENT_STATUS_PENDING_REVIEW.equals(comment.getStatus()));

        if (hasInvalidStatus) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR,
                    "所选评论中含有状态为'正常'或'审核未通过'评论，请重新选择!");
        }

        //批量更新评论状态为审核未通过
        commentsToReject.forEach(comment -> comment.setStatus(SystemConstants.COMMENT_STATUS_REVIEW_NOT_PASSED));

        //先更新数据库
        boolean updateResult = commentService.updateBatchById(commentsToReject);
        if (!updateResult) {
            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "审核失败");
        }

        //数据库更新成功后再发送审核未通过邮件
        commentsToReject.forEach(comment -> {
            if (!StringUtils.hasText(comment.getEmail())) {
                return;
            }

            try {
                emailService.sendCommentRejectedNotification(comment);
            } catch (Exception e) {
                log.error("发送评论审核未通过邮件失败，commentId={}, email={}", comment.getId(), comment.getEmail(), e);
            }
        });

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode(),
                "成功拒绝 " + commentsToReject.size() + " 条评论进行发布");
    }

    //获取评论统计信息
    @Override
    public ResponseResult statisticsComment()
    {
        //查询评论总数
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        Long total = commentMapper.selectCount(queryWrapper);
        
        //查询待审核评论数
        LambdaQueryWrapper<Comment> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(Comment::getStatus, SystemConstants.COMMENT_STATUS_PENDING_REVIEW);
        Long pendingReview = commentMapper.selectCount(pendingWrapper);
        
        //查询审核未通过评论数
        LambdaQueryWrapper<Comment> rejectedWrapper = new LambdaQueryWrapper<>();
        rejectedWrapper.eq(Comment::getStatus, SystemConstants.COMMENT_STATUS_REVIEW_NOT_PASSED);
        Long rejected = commentMapper.selectCount(rejectedWrapper);
        
        //查询今日评论数
        //获取今天的开始时间（00:00:00）
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        Date todayStartDate = Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant());
        
        //获取今天的结束时间（23:59:59）
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Date todayEndDate = Date.from(todayEnd.atZone(ZoneId.systemDefault()).toInstant());

        //根据一天时间统计今天的评论数量
        LambdaQueryWrapper<Comment> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(Comment::getCreateTime, todayStartDate)
                    .le(Comment::getCreateTime, todayEndDate);
        Long todayCount = commentMapper.selectCount(todayWrapper);
        
        //转化未Vo
        CommentStatisticsVo statisticsVo = new CommentStatisticsVo(total, pendingReview, todayCount, rejected);

        return ResponseResult.okResult(statisticsVo);
    }

    //导出评论数据列表
    @Override
    public ResponseResult exportComment(Integer pageNum, Integer pageSize, CommentListDto commentListDto)
    {
        //构建查询条件
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        
        //关键字模糊搜索
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
        
        //根据是否传入分页参数决定查询方式
        List<Comment> comments;
        if (pageNum != null && pageSize != null && pageNum > 0 && pageSize > 0) {
            //分页查询
            Page<Comment> page = new Page<>(pageNum, pageSize);
            commentMapper.selectPage(page, queryWrapper);
            comments = page.getRecords();
        } else {
            //查询所有符合条件的评论
            comments = commentMapper.selectList(queryWrapper);
        }
        
        //转换为导出VO
        List<CommentExportVo> exportData = toCommentExportVoList(comments);
        
        //返回导出数据
        return ResponseResult.okResult(exportData);
    }

    //删除评论（逻辑删除）
    @Override
    @Transactional
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
        if (commentsToDelete.size() != ids.length) {
            //找出不存在的 评论id
            Set<Long> existIds = commentsToDelete.stream()
                    .map(Comment::getId)
                    .collect(Collectors.toSet());

            List<Long> notExistIds = idList.stream()
                    .filter(id -> !existIds.contains(id))
                    .toList();

            return ResponseResult.errorResult(AppHttpCodeEnum.SYSTEM_ERROR, "删除失败！！原因：以下评论 id 不存在：" + notExistIds);
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
    
    //转换为CommentExportVo列表
    private List<CommentExportVo> toCommentExportVoList(List<Comment> comments)
    {
        if (comments == null || comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        //批量查询文章标题
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
        
        //转换为导出VO
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<CommentExportVo> exportList = new ArrayList<>();
        
        for (Comment comment : comments)
        {
            CommentExportVo vo = new CommentExportVo();
            vo.setId(String.valueOf(comment.getId()));
            
            //评论类型转换为中文
            String typeName = getCommentTypeName(comment.getType());
            vo.setTypeName(typeName);
            
            //设置文章标题
            if (comment.getArticleId() != null && comment.getArticleId() > 0) {
                vo.setArticleTitle(articleTitleMap.get(comment.getArticleId()));
            }
            
            //设置用户信息
            vo.setNickname(comment.getNickname());
            vo.setEmail(comment.getEmail());
            vo.setPersonalWebsite(comment.getPersonalWebsite());
            
            //设置评论内容
            vo.setContent(comment.getContent());
            
            //设置回复目标昵称
            if (comment.getReplyToCommentId() != null && comment.getReplyToCommentId() > 0) {
                vo.setReplyToNickname(replyToNicknameMap.get(comment.getReplyToCommentId()));
            }
            
            vo.setLikeCount(comment.getLikeCount());
            
            //审核状态转换为中文
            String statusName = getCommentStatusName(comment.getStatus());
            vo.setStatusName(statusName);
            
            vo.setCreateTime(sdf.format(comment.getCreateTime()));
            
            exportList.add(vo);
        }
        
        return exportList;
    }
    
    //获取评论类型名称
    private String getCommentTypeName(String type)
    {
        if (SystemConstants.COMMENT_TYPE_ARTICLE.equals(type)) {
            return "文章评论";
        } else if (SystemConstants.COMMENT_TYPE_LINK.equals(type)) {
            return "友链评论";
        } else if (SystemConstants.COMMENT_TYPE_MESSAGE.equals(type)) {
            return "留言板评论";
        }
        return "未知类型";
    }
    
    //获取评论状态名称
    private String getCommentStatusName(String status)
    {
        if (SystemConstants.COMMENT_STATUS_NORMAL.equals(status)) {
            return "正常";
        } else if (SystemConstants.COMMENT_STATUS_PENDING_REVIEW.equals(status)) {
            return "待审核";
        } else if (SystemConstants.COMMENT_STATUS_REVIEW_NOT_PASSED.equals(status)) {
            return "审核未通过";
        }
        return "未知状态";
    }
}
