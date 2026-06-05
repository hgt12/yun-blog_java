package com.mengze.constants;

/**
 * 返回结果相关 常量
 */
public class ResultPutConstants
{
    private ResultPutConstants() {}

    //认证返回结果相关
    /**  验证码id */
    public static final String CAPTCHA_ID = "captchaId";
    /**  验证码 Base64 图 */
    public static final String CAPTCHA_BASE64 = "captchaBase64";

    //导出返回结果相关
    /**  有效数量 */
    public static final String VALID_COUNT = "validCount";
    /**  无效数量 */
    public static final String INVALID_COUNT = "invalidCount";
    /**  消息列表 */
    public static final String MESSAGE_LIST = "messageList";

    //评论头像返回结果相关
    /**  返回评论头像 */
    public static final String AVATAR = "avatar";
}
