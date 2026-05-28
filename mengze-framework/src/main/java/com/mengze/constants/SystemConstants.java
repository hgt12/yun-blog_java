package com.mengze.constants;

//常量类，后面有需要的常量都可以在这里写
public class SystemConstants
{
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
     *  评论状态：正常评论
     */
    public static final String COMMENT_STATUS_NORMAL = "0";
    /**
     *  评论状态：待审核
     */
    public static final String COMMENT_STATUS_PENDING_REVIEW = "1";
    /**
     *  评论状态：审核未通过
     */
    public static final String COMMENT_STATUS_REVIEW_NOT_PASSED = "2";
    /**
     *  允许评论
     */
    public static final String ALLOW_COMMENT = "1";
    /**
     *  不允许评论
     */
    public static final String NOT_ALLOWED_COMMENT = "0";
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
     *  全站访客去重 Set key
     */
    public static final String SITE_VISITOR_SET_PREFIX = "site:visitor:set:all";
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
    /**
     *  目录始终显示
     */
    public static final String CONTENTS_ALWAYS_SHOW = "0";
    /**
     *  目录不显示
     */
    public static final String CONTENTS_HIDE = "1";
    /**
     *  缓存 keepAlive
     */
    public static final String CACHE_KEEP_ALIVE = "0";
    /**
     *  不缓存 keepAlive
     */
    public static final String NOT_CACHE_KEEP_ALIVE = "1";
    /**
     *  默认作者名称
     */
    public static final String DEFAULT_AUTHOR = "云梦泽";
    /**
     *  自定义部门数据权限
     */
    public static final Integer CUSTOM_DEPT_DATA_SCOPE = 5;
    /**
     *  操作人为y游客
     */
    public static final String OPERATOR_IS_VISITOR = "游客";
    /**
     *  未知 Unknown
     */
    public static final String UNKNOWN = "unknown";
    /**
     *  用户状态：正常
     */
    public static final String USER_STATUS_NORMAL = "0";
    /**
     *  用户状态：已封禁
     */
    public static final String USER_STATUS_BANNED = "1";
    /**
     *  性别：弟弟
     */
    public static final String SEX_MAN = "0";
    /**
     *  性别：妹妹
     */
    public static final String SEX_WOMEN = "1";
    /**
     *  性别：隐藏
     */
    public static final String SEX_HIDE = "2";
    /**
     *  登录类型：邮箱
     */
    public static final String LOGIN_TYPE_EMAIL = "email";
    /**
     *  登录类型：手机号
     */
    public static final String LOGIN_TYPE_PHONE = "phone";
    /**
     *  验证码类型：邮箱
     */
    public static final String CODE_TYPE_EMAIL = "email";
    /**
     *  验证码类型：手机号
     */
    public static final String CODE_TYPE_PHONE = "phone";
    /**
     *  验证码类型：手机
     */
    public static final String CODE_TYPE_MOBILE = "mobile";
    /**
     *  验证类型：注册
     */
    public static final String VERIFY_TYPE_LOGIN = "login";
    /**
     *  验证类型：登录
     */
    public static final String VERIFY_TYPE_REGISTER = "register";
    /**
     *  验证类型：忘记密码
     */
    public static final String VERIFY_TYPE_FORGOT_PASSWORD = "forgot_password";
    /**
     *  父节点路径：Parent Node Path
     */
    public static final String PARENT_NODE_PATH = "0";
    /**
     *  通知未读
     */
    public static final String NOTICE_NOT_READ = "0";
    /**
     *  目标类型：全体成员
     */
    public static final String TARGET_TYPE_IS_ALL = "1";
    /**
     *  目标类型：指定用户
     */
    public static final String TARGET_TYPE_IS_SPECIFIED = "2";
    /**
     *  通知状态：已发布
     */
    public static final String NOTICE_STATUS_PUBLISHED = "1";
}
