package com.mengze.enums;

import lombok.Getter;

//枚举类
@Getter
public enum AppHttpCodeEnum {
    // 成功
    SUCCESS(200, "操作成功"),
    // 登录
    NEED_LOGIN(401, "需要登录后操作"),
    NO_OPERATOR_AUTH(403, "无权限操作"),
    SYSTEM_ERROR(500, "出现错误"),
    USERNAME_EXIST(1001, "用户名已存在"),
    PHONENUMBER_EXIST(1002, "手机号已存在"),
    EMAIL_EXIST(1003, "邮箱已存在"),
    REQUIRE_USERNAME(1004, "必需填写用户名"),
    LOGIN_ERROR(1005, "用户名或密码错误"),
    CONTENT_NOT_NULL(1006, "评论内容不能为空"),
    NICKNAME_NOT_NULL(1007, "昵称不能为空"),
    EMAIL_NOT_NULL(1008, "邮箱不能为空"),
    USERNAME_NOT_NULL(1009, "用户名不能为空"),
    PASSWORD_NOT_NULL(1010, "密码不能为空"),
    EMAIL_OCCUPIED(1011, "邮箱被占用"),
    USERNAME_OCCUPIED(1012, "用户名被占用"),
    PHONE_OCCUPIED(1013, "电话号码被占用"),
    NICKNAME_OCCUPIED(1014, "昵称被占用"),
    FILE_TYPE_ERROR(1015, "文件类型错误！请上传.png或.jpg或.gif格式的文件！"),
    COMMENT_NOT_EXIST(1016, "评论不存在"),
    PHONE_FORMAT_ERROR(1017, "电话号码格式错误"),
    FILE_SIZE_ERROR(1018, "文件大小超过限制！请上传小于2MB的图片！"),
    SMS_SEND_FREQUENTLY(1019, "验证码发送过于频繁，请稍后再试"),
    IP_REQUEST_LIMIT(1020, "请求过于频繁，请稍后再试"),
    VERIFICATION_CODE_ERROR(1021, "验证码错误或已过期"),
    SELECT_ERROR(1022, "未查到信息"),
    CAPTCHA_ERROR(1023, "图形验证码错误"),
    CAPTCHA_EXPIRED(1024, "图形验证码已过期")
    ;
    int code;
    String msg;

    AppHttpCodeEnum(int code, String errorMessage) {
        this.code = code;
        this.msg = errorMessage;
    }

}