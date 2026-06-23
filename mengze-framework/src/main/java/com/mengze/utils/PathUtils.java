package com.mengze.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 文件存储工具类
 */
public class PathUtils
{
    public static String generateFilePath(String fileName){
        // 根据日期生成路径，例如：2026/05/07/
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
        String datePath = sdf.format(new Date());
        // uuid作为文件名后缀
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        // 将空格和特殊字符统一替换成下划线，避免 URL / OSS key 解析问题
        String safeFileName = sanitizeFileName(fileName);
        return new StringBuilder()
                .append(datePath)
                .append(uuid)
                .append("_")
                .append(safeFileName)
                .toString();
    }


    // 文件名清洗规则：
    // 去掉首尾空白字符
    // 将所有连续空白字符（空格、Tab、换行等）替换为下划线
    // 将除字母、数字、点号、下划线、连字符以外的字符统一替换为下划线
    // 将连续多个下划线压缩为一个下划线
    // 简单的说就是转换之后的文件名只包含：中文、英文、其他语言字母，数字，.，-，_
    private static String sanitizeFileName(String fileName) {
        if (fileName == null) {
            return "";
        }

        return fileName.trim()
                .replaceAll("\\s+", "_")
                .replaceAll("[^\\p{L}\\p{N}._-]", "_")
                .replaceAll("_+", "_");
    }
}

