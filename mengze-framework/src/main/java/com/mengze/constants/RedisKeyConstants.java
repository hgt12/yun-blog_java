package com.mengze.constants;

/**
 * Redis Key 常量
 */
public class RedisKeyConstants
{
    private RedisKeyConstants() {}

    //后台认证相关
    /** 后台用户登录认证信息 Redis Key 前缀 */
    public static final String ADMIN_LOGIN_KEY_PREFIX = "adminLogin:";
    /** 验证码 Redis Key 前缀 */
    public static final String CAPTCHA_KEY_PREFIX = "captcha:";

    //配置相关
    /** 配置 Redis Key 前缀 */
    public static final String CONFIG_KEY_PREFIX = "config:";

    //后台用户相关
    /** 验证码发送时间(后台) Redis Key 模板 */
    public static final String ADMIN_CODE_SEND_TIME_KEY_TEMPLATE = "admin:code:send:time:%s";
    /** 验证码(后台) Redis Key 模板 */
    public static final String ADMIN_CODE_KEY_TEMPLATE = "admin:code:%s";
    /** 每日 同IP 限制次数(后台) Redis Key 模板 */
    public static final String ADMIN_IP_LIMIT_DAY_KEY_TEMPLATE = "admin:ip:limit:day:%s";
    /** 每分钟 同IP 限制次数(后台) Redis Key 模板 */
    public static final String ADMIN_IP_LIMIT_MINUTE_KEY_TEMPLATE = "admin:ip:limit:minute:%s";
    /** 每小时 同IP 限制次数(后台) Redis Key 模板 */
    public static final String ADMIN_IP_LIMIT_HOUR_KEY_TEMPLATE = "admin:ip:limit:hour:%s";

    //前台用户认证相关
    /** 验证码发送时间(前台) Redis Key 模板 */
    public static final String CODE_SEND_TIME_KEY_TEMPLATE = "%s:code:send:time:%s:%s";
    /** 验证码(前台) Redis Key 模板 */
    public static final String CODE_KEY_TEMPLATE = "%s:code:%s:%s";
    /** 每分钟 同IP 限制次数(前台) Redis Key 前缀 */
    public static final String AUTH_IP_LIMIT_MINUTE_KEY_PREFIX = "auth:ip:limit:minute:";
    /** 每小时 同IP 限制次数(前台) Redis Key 前缀 */
    public static final String AUTH_IP_LIMIT_HOUR_KEY_PREFIX = "auth:ip:limit:hour:";
    /** 每天 同IP 限制次数(前台) Redis Key 前缀 */
    public static final String AUTH_IP_LIMIT_DAY_KEY_PREFIX = "auth:ip:limit:day:";

    //前台登录相关
    /** 前台登录 Redis Key 前缀 */
    public static final String BLOG_LOGIN_KEY_PREFIX = "blogLogin:";
    /** 前台登录邮箱验证码 Redis Key 前缀 */
    public static final String LOGIN_CODE_EMAIL_KEY_PREFIX = "login:code:email:" ;
    /** 前台登录手机验证码 Redis Key 前缀 */
    public static final String LOGIN_CODE_PHONE_KEY_PREFIX = "login:code:phone:" ;

    //前台注册相关
    /** 前台注册邮箱验证码 Redis Key 前缀 */
    public static final String REGISTER_CODE_EMAIL_KEY_PREFIX = "register:code:email:" ;
    /** 前台注册手机验证码 Redis Key 前缀 */
    public static final String REGISTER_CODE_PHONE_KEY_PREFIX = "register:code:phone:" ;

    //Oss 相关
    /** 临时文件 Redis Key 前缀 */
    public static final String TEMP_FILE_KEY_PREFIX = "temp:file:";

    //前台重置密码相关
    /** 忘记/重置密码邮箱验证码 Redis Key 前缀 */
    public static final String FORGOT_PASSWORD_CODE_EMAIL_KEY_PREFIX = "forgot_password:code:email:";

    //天气相关
    /**  Redis Key 前缀 */
    public static final String WEATHER_NOW_KEY_PREFIX = "weather:now:";

    //WebSocket 相关
    /** 在线用户统计 Redis Key 前缀 */
    public static final String WS_ONLINE_USERS_KEY_PREFIX = "ws:online:users:";
    /** 在线会话集合 Redis Key */
    public static final String WS_ONLINE_SESSIONS_KEY = "ws:online:sessions";
    /** 用户会话集合 Redis Key 前缀 */
    public static final String WS_ONLINE_USER_SESSIONS_KEY_PREFIX = "ws:online:user:sessions:";

    //浏览量相关
    /** 文章预览量增量 Redis Key */
    public static final String ARTICLE_VIEW_COUNT_INCREMENT_KEY = "article:viewCount:increment";
    /** 网站 PV 增量 Redis key */
    public static final String SITE_VIEWS_ADD_INCREMENT_KEY = "site:views:increment";
    /** 网站 UV 增量 Redis key */
    public static final String SITE_VISITORS_INCREMENT_KEY = "site:visitors:increment";
    /** 全站访客去重 Set key */
    public static final String SITE_VISITOR_SET_KEY = "site:visitor:set:all";

    //日志相关
    /** 访问日志队列 key */
    public static final String VISIT_LOG_QUEUE_KEY = "site:visitLog:queue";

    //图形验证码相关
    /**
     * 二次验证 Token 在 Redis 中的 Key 前缀
     * 与 yml 中 captcha.secondary.keyPrefix 配置保持一致
     */
    public static final String SECONDARY_TOKEN_KEY_PREFIX = "captcha:secondary:";
    /** Token 有效期（分钟），与 yml 中 captcha.secondary.expire 配置保持一致 */
    public static final Integer TOKEN_EXPIRE_MINUTES = 2;

}
