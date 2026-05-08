package com.mengze.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 文件存储工具类
 */
public class PathUtils {
    public static String generateFilePath(String fileName){
        // 根据日期生成路径，例如：2026/05/07/
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");
        String datePath = sdf.format(new Date());
        // uuid作为文件名后缀
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        // 保留原始文件名，并使用下划线拼接uuid，便于和展示名区分
        return new StringBuilder()
                .append(datePath)
                .append(uuid)
                .append("_")
                .append(fileName)
                .toString();
    }
}

