package com.mengze.utils;

import org.springframework.web.util.HtmlUtils;

/**
 * XSS防护工具类
 * 用于过滤用户输入中的恶意脚本
 * 
 * @author mengze
 */
public class XssUtils {

    /**
     * HTML转义，防止XSS攻击
     * 
     * @param input 原始输入
     * @return 转义后的安全字符串
     */
    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return HtmlUtils.htmlEscape(input);
    }

    /**
     * 批量HTML转义
     * 
     * @param inputs 原始输入数组
     * @return 转义后的安全字符串数组
     */
    public static String[] escapeHtml(String... inputs) {
        if (inputs == null || inputs.length == 0) {
            return inputs;
        }
        
        String[] escaped = new String[inputs.length];
        for (int i = 0; i < inputs.length; i++) {
            escaped[i] = escapeHtml(inputs[i]);
        }
        return escaped;
    }

    /**
     * 清理危险字符，保留基本格式
     * 适用于需要保留部分HTML格式的场景（如富文本编辑器）
     * 
     * @param input 原始输入
     * @return 清理后的字符串
     */
    public static String cleanDangerousChars(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        // 移除script标签及其内容
        input = input.replaceAll("(?i)<script[^>]*>.*?</script>", "");
        
        // 移除iframe标签
        input = input.replaceAll("(?i)<iframe[^>]*>.*?</iframe>", "");
        
        // 移除on事件处理器（如onclick, onerror等）
        input = input.replaceAll("(?i)\\s*on\\w+\\s*=\\s*[\"'][^\"']*[\"']", "");
        input = input.replaceAll("(?i)\\s*on\\w+\\s*=\\s*[^\\s>]*", "");
        
        // 移除javascript:伪协议
        input = input.replaceAll("(?i)javascript:", "");
        
        // 移除vbscript:伪协议
        input = input.replaceAll("(?i)vbscript:", "");
        
        return input;
    }

    /**
     * 验证URL是否安全
     * 防止javascript:、data:等危险协议
     * 
     * @param url 待验证的URL
     * @return true-安全，false-不安全
     */
    public static boolean isSafeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return true;
        }
        
        String lowerUrl = url.toLowerCase().trim();
        
        // 检查危险协议
        String[] dangerousProtocols = {
            "javascript:", "data:", "vbscript:", "file:", "about:"
        };
        
        for (String protocol : dangerousProtocols) {
            if (lowerUrl.startsWith(protocol)) {
                return false;
            }
        }
        
        return true;
    }
}
