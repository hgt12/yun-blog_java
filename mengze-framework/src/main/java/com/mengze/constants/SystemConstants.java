package com.mengze.constants;

//常量类，后面有需要的常量都可以在这里写
public class SystemConstants
{
    //下面字段对应数据库里的字段
    /**
     *  文章是草稿
     */
    public static final String ARTICLE_STATUS_DRAFT = "1";
    /**
     *  文章是正常发布状态
     */
    public static final String ARTICLE_STATUS_NORMAL = "0";
    /**
     *  正常状态
     */
    public static final String STATUS_NORMAL = "0";
    /**
     *  友链审核通过状态
     */
    public static final String LINK_STATUS_NORMAL = "0";
    /**
     *  评论类型：文章评论
     */
    public static final String COMMENT_TYPE_ARTICLE = "0";
    /**
     *  评论类型：友链评论
     */
    public static final String COMMENT_TYPE_LINK = "1";
    /**
     *  评论类型：留言板评论
     */
    public static final String COMMENT_TYPE_MESSAGE = "2";
    /**
     *  评论状态：正常文章评论
     */
    public static final String COMMENT_STATUS_NORMAL = "0";
    /**
     *  评论状态待审核
     */
    public static final String COMMENT_STATUS_PENDING_REVIEW = "1";
    /**
     *  允许评论
     */
    public static final String ALLOW_COMMENT = "0";
    /**
     *  不允许评论
     */
    public static final String NOT_ALLOWED_COMMENT = "1";
    /**
     *  根评论标识（rootId为-1表示是根评论）
     */
    public static final Long COMMENT_ROOT_ID = -1L;
    /**
     *  博主邮箱（用于匿名评论时识别博主身份）
     */
    public static final String BLOGGER_EMAIL = "2962933152@qq.com";
    /**
     *  文章预览量
     */
    public static final String ARTICLE_VIEW_COUNT = "article:viewCount";
    /**
     *  网站总预览量（PV）Redis key
     */
    public static final String SITE_TOTAL_VIEWS = "site:totalViews";
    /**
     *  网站总预览量 Hash field
     */
    public static final String SITE_TOTAL_VIEWS_FIELD = "total";
    /**
     *  网站总访客数（UV）Redis key
     */
    public static final String SITE_TOTAL_VISITORS = "site:totalVisitors";
    /**
     *  网站总访客数 Hash field
     */
    public static final String SITE_TOTAL_VISITORS_FIELD = "total";
    /**
     *  访客去重 Set key 前缀（按月分 key，格式：site:visitor:set:yyyy-MM）
     */
    public static final String SITE_VISITOR_SET_PREFIX = "site:visitor:set:";
    /**
     *  访问日志队列 key
     */
    public static final String VISIT_LOG_QUEUE = "site:visitLog:queue";
    /**
     *  菜单类型为目录
     */
    public static final String MENU_TYPE_C = "C";
    /**
     *  菜单类型为菜单
     */
    public static final String MENU_TYPE_M = "M";
    /**
     *  菜单类型为按钮
     */
    public static final String MENU_TYPE_B = "B";
    /**
     *  角色为超级管理员
     */
    public static final String ROLE_IS_ADMIN = "admin";
    /**
     *  角色为用户
     */
    public static final String ROLE_IS_USER = "user";
    /**
     *  角色为访客
     */
    public static final String ROLE_IS_VISITOR = "visitor";
    /**
     *  默认评论点赞量
     */
    public static final int DEFAULT_COMMENT_LIKES = 0;
    /**
     *  可见
     */
    public static final String VISIBLE = "0";
    /**
     *  不可见
     */
    public static final String INVISIBLE = "1";

}
