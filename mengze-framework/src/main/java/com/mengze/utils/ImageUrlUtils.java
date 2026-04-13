package com.mengze.utils;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 图片URL处理工具类
 * 用于处理文章内容和富文本中的图片URL
 * 
 * @author mengze
 */
public class ImageUrlUtils
{
    // 匹配临时图片URL的正则表达式
    private static final String TEMP_IMAGE_REGEX = "(https?://[^\\s\"']+/temp/[^\\s\"']+)";
    private static final Pattern TEMP_IMAGE_PATTERN = Pattern.compile(TEMP_IMAGE_REGEX);
    
    // 匹配所有图片URL的正则表达式
    private static final String IMAGE_URL_REGEX = "(https?://[^\\s\"']+\\.(jpg|jpeg|png|gif|webp|bmp))";
    private static final Pattern IMAGE_URL_PATTERN = Pattern.compile(IMAGE_URL_REGEX, Pattern.CASE_INSENSITIVE);
    
    /**
     * 从内容中提取所有图片URL
     * 
     * @param content 富文本内容（HTML、Markdown等）
     * @return 图片URL列表
     */
    public static List<String> extractImageUrls(String content)
    {
        List<String> imageUrls = new ArrayList<>();
        
        if (!StringUtils.hasText(content)) {
            return imageUrls;
        }
        
        Matcher matcher = IMAGE_URL_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String imageUrl = matcher.group(1);
            imageUrls.add(imageUrl);
        }
        
        return imageUrls;
    }
    
    /**
     * 从内容中提取所有临时图片URL
     * 
     * @param content 富文本内容
     * @return 临时图片URL列表
     */
    public static List<String> extractTempImageUrls(String content)
    {
        List<String> tempImageUrls = new ArrayList<>();
        
        if (!StringUtils.hasText(content)) {
            return tempImageUrls;
        }
        
        Matcher matcher = TEMP_IMAGE_PATTERN.matcher(content);
        
        while (matcher.find()) {
            String tempUrl = matcher.group(1);
            tempImageUrls.add(tempUrl);
        }
        
        return tempImageUrls;
    }
    
    /**
     * 检查内容中是否包含临时图片
     * 
     * @param content 富文本内容
     * @return 是否包含临时图片
     */
    public static boolean containsTempImages(String content)
    {
        if (!StringUtils.hasText(content)) {
            return false;
        }
        
        return content.contains("temp/");
    }
    
    /**
     * 替换内容中的图片URL
     * 
     * @param content 原始内容
     * @param oldUrl 旧的图片URL
     * @param newUrl 新的图片URL
     * @return 替换后的内容
     */
    public static String replaceImageUrl(String content, String oldUrl, String newUrl)
    {
        if (!StringUtils.hasText(content) || !StringUtils.hasText(oldUrl)) {
            return content;
        }
        
        return content.replace(oldUrl, newUrl);
    }
}
