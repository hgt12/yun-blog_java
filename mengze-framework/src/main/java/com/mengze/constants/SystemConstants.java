package com.mengze.constants;

//常量类，后面有需要的常量都可以在这里写
public class SystemConstants
{
    //下面字段对应数据库里的字段
    /**
     *  文章是草稿
     */
    public static final int ARTICLE_STATUS_DRAFT = 1;
    /**
     *  文章是正常发布状态
     */
    public static final int ARTICLE_STATUS_NORMAL = 0;
    /**
     *  文章是正常发布状态字符串型
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
     *  根评论标识（rootId为-1表示是根评论）
     */
    public static final Long COMMENT_ROOT_ID = -1L;
    /**
     *  博主邮箱（用于匿名评论时识别博主身份）
     */
    public static final String BLOGGER_EMAIL = "2962933152@qq.com";
}
