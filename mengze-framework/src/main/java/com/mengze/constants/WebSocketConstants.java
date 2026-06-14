package com.mengze.constants;

/**
 * WebSocket 相关常量
 */
public final class WebSocketConstants
{
    //防止被误当成普通对象来创建
    private WebSocketConstants() {}

    /** 管理后台登录类型标识 */
    public static final String LOGIN_TYPE_ADMIN = "admin";
    /** 博客前台登录类型标识 */
    public static final String LOGIN_TYPE_BLOG = "blog";
    /** 游客登录类型标识 */
    public static final String LOGIN_TYPE_VISITOR = "visitor";
    /** WebSocket CONNECT 中携带的 token 请求头 */
    public static final String HEADER_TOKEN = "token";
    /** WebSocket CONNECT 中携带的访客 ID 请求头 */
    public static final String HEADER_VISITOR_ID = "visitorId";
    /** WebSocket CONNECT 中携带的登录类型请求头 */
    public static final String HEADER_LOGIN_TYPE = "loginType";
    /** WebSocket CONNECT 中携带的登录用户请求头 */
    public static final String HEADER_LOGIN_USER = "loginUser";
    /** WebSocket CONNECT 中携带的认证信息请求头 */
    public static final String HEADER_AUTHENTICATION = "authentication";

    /** 管理后台在线人数订阅主题 */
    public static final String TOPIC_ADMIN_ONLINE_COUNT = "/topic/admin/online-count";
    /** 博客前台在线人数订阅主题 */
    public static final String TOPIC_BLOG_ONLINE_COUNT = "/topic/blog/online-count";
    /** 游客在线人数订阅主题 */
    public static final String TOPIC_VISITOR_ONLINE_COUNT = "/topic/blog/visitor/online-count";

    /** WebSocket 端点类型 - 后台 */
    public static final String ENDPOINT_TYPE_ADMIN = "admin";
    /** WebSocket 端点类型 - 前台 */
    public static final String ENDPOINT_TYPE_BLOG = "blog";
    /** 握手阶段保存端点类型的属性名 */
    public static final String ATTR_ENDPOINT_TYPE = "webSocketEndpointType";

    /** 默认登录类型 */
    public static final String DEFAULT_LOGIN_TYPE = LOGIN_TYPE_ADMIN;
}
