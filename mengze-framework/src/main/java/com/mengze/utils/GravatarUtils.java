package com.mengze.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 头像工具类
 * 支持 QQ 邮箱直接获取 QQ 头像，其他邮箱使用 Gravatar
 */
public class GravatarUtils
{

    // 使用国内可访问的 Gravatar 镜像（loli.net 较稳定）或（https://cravatar.cn/avatar/）（https://gravatar.loli.net/avatar/）
    private static final String GRAVATAR_URL = "https://cravatar.cn/avatar/";
    // QQ 头像接口
    private static final String QQ_AVATAR_URL = "https://q1.qlogo.cn/g?b=qq&nk=";
    // 默认头像类型
    private static final String DEFAULT_AVATAR = "identicon";
    // 默认头像大小
    private static final int DEFAULT_SIZE = 80;

    /**
     * 根据邮箱生成头像 URL
     * QQ 邮箱直接获取 QQ 头像，其他邮箱使用 Gravatar
     * @param email 邮箱地址
     * @return 头像 URL
     */
    public static String getGravatarUrl(String email) {
        return getGravatarUrl(email, DEFAULT_SIZE);
    }

    /**
     * 根据邮箱生成指定大小的头像 URL
     * @param email 邮箱地址
     * @param size 头像大小
     * @return 头像 URL
     */
    public static String getGravatarUrl(String email, int size) {
        if (email == null || email.trim().isEmpty()) {
            return GRAVATAR_URL + "00000000000000000000000000000000?d=" + DEFAULT_AVATAR + "&s=" + size;
        }
        
        String trimmedEmail = email.trim().toLowerCase();
        
        // QQ 邮箱直接使用 QQ 头像
        if (trimmedEmail.endsWith("@qq.com")) {
            String qqNumber = trimmedEmail.replace("@qq.com", "");
            // 验证是否为纯数字（QQ号）
            if (qqNumber.matches("\\d+")) {
                return QQ_AVATAR_URL + qqNumber + "&s=" + size;
            }
        }
        
        // 其他邮箱使用 Gravatar
        String hash = md5Hex(trimmedEmail);
        return GRAVATAR_URL + hash + "?d=" + DEFAULT_AVATAR + "&s=" + size;
    }

    /**
     * 计算字符串的 MD5 哈希值
     * @param input 输入字符串
     * @return MD5 哈希值（小写十六进制）
     */
    private static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not found", e);
        }
    }
}
