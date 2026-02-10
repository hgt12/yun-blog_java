package com.mengze.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 头像工具类
 * 使用 Gravatar 生成头像
 */
public class GravatarUtils
{

    // 使用国内可访问的 Gravatar 镜像
    private static final String GRAVATAR_URL = "https://cravatar.cn/avatar/";
    // 默认头像类型（mp: 神秘人, identicon: 几何图案, monsterid: 怪物, wavatar: 卡通脸, retro: 8位像素, robohash: 机器人）
    private static final String DEFAULT_AVATAR = "mp";
    // 评论区默认头像类型（彩色几何图案）
    private static final String COMMENT_DEFAULT_AVATAR = "identicon";
    // 默认头像大小
    private static final int DEFAULT_SIZE = 80;

    /**
     * 根据邮箱生成头像 URL
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
    public static String getGravatarUrl(String email, int size)
    {
        if (email == null || email.trim().isEmpty()) {
            return GRAVATAR_URL + "00000000000000000000000000000000?d=" + DEFAULT_AVATAR + "&s=" + size;
        }
        
        String trimmedEmail = email.trim().toLowerCase();
        
        // 统一使用 Gravatar（更稳定可靠）
        String hash = md5Hex(trimmedEmail);
        return GRAVATAR_URL + hash + "?d=" + DEFAULT_AVATAR + "&s=" + size;
    }

    /**
     * 根据邮箱生成评论框预览头像 URL
     * 规则：
     * - 空邮箱：使用 mp（神秘人物轮廓）
     * - 邮箱格式错误：使用 identicon（彩色几何图案）
     * - 邮箱格式正确：使用 identicon 作为默认头像
     * 
     * @param email 邮箱地址
     * @return 头像 URL
     */
    public static String getCommentPreviewAvatarUrl(String email)
    {
        return getCommentPreviewAvatarUrl(email, DEFAULT_SIZE);
    }

    /**
     * 根据邮箱生成评论框预览头像 URL
     * @param email 邮箱地址
     * @param size 头像大小
     * @return 头像 URL
     */
    public static String getCommentPreviewAvatarUrl(String email, int size)
    {
        // 空邮箱：使用 mp（神秘人物轮廓）
        if (email == null || email.trim().isEmpty()) {
            return GRAVATAR_URL + "00000000000000000000000000000000?d=" + DEFAULT_AVATAR + "&s=" + size;
        }
        
        String trimmedEmail = email.trim().toLowerCase();
        
        // 验证邮箱格式
        if (!isValidEmail(trimmedEmail)) {
            // 邮箱格式错误：使用 identicon（彩色几何图案）
            return GRAVATAR_URL + "00000000000000000000000000000000?d=" + COMMENT_DEFAULT_AVATAR + "&s=" + size;
        }
        
        // 邮箱格式正确：使用 identicon 作为默认头像
        String hash = md5Hex(trimmedEmail);
        return GRAVATAR_URL + hash + "?d=" + COMMENT_DEFAULT_AVATAR + "&s=" + size;
    }

    /**
     * 验证邮箱格式
     * @param email 邮箱地址
     * @return 是否为有效邮箱
     */
    private static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        // 简单的邮箱格式验证
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
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
