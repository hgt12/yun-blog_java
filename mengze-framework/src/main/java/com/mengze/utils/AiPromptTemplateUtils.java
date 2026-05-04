package com.mengze.utils;

/**
 * AI提示词模板工具类
 * @author mengze
 */
public class AiPromptTemplateUtils {

    /**
     * 敏感词检测提示词模板
     */
    private static final String SENSITIVE_CHECK_TEMPLATE = 
            "请严格判断以下内容是否包含敏感信息（暴力、色情、反动、诱导等）和包括不限于近期网络梗的违规敏感词，中文拼音首字母缩写和英文单词等敏感信息，" +
            "用JSON格式回答：{\"sensitive\":布尔值, \"confidence\":0-1的置信度}\n" +
            "内容：%s";

    /**
     * 构建敏感词检测提示词
     * @param content 待检测内容
     * @return 完整的提示词
     */
    public static String buildSensitiveCheckPrompt(String content) {
        return String.format(SENSITIVE_CHECK_TEMPLATE, content);
    }

    /**
     * 构建自定义检测提示词
     * @param content 待检测内容
     * @param customRules 自定义规则描述
     * @return 完整的提示词
     */
    public static String buildCustomCheckPrompt(String content, String customRules) {
        String template = "请根据以下规则判断内容：%s\n" +
                "用JSON格式回答：{\"sensitive\":布尔值, \"confidence\":0-1的置信度, \"reason\":\"原因\"}\n" +
                "内容：%s";
        return String.format(template, customRules, content);
    }

    /**
     * 构建内容质量评估提示词
     * @param content 待评估内容
     * @return 完整的提示词
     */
    public static String buildQualityCheckPrompt(String content) {
        String template = "请评估以下内容的质量（是否为垃圾信息、广告、无意义内容等），" +
                "用JSON格式回答：{\"isSpam\":布尔值, \"confidence\":0-1的置信度, \"category\":\"分类\"}\n" +
                "内容：%s";
        return String.format(template, content);
    }
}
