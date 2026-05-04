package com.mengze.utils;

import com.github.houbb.heaven.util.lang.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;

/**
 * 敏感词校验工具类
 * 支持多文件加载、并行加载
 */
@Slf4j
@Component
public class SensitiveUtil
{

    /**
     * 敏感词文件目录（支持通配符）
     */
    private static final String SENSITIVE_WORD_PATTERN = "classpath:sensitive.word.database/*.txt";

    /**
     * 根节点
     */
    private static final TrieNode ROOT_NODE = new TrieNode();

    /**
     * 敏感词总数
     */
    private static final AtomicInteger WORD_COUNT = new AtomicInteger(0);

    /**
     * 敏感词与来源文件的映射关系
     */
    private static final Map<String, String> WORD_SOURCE_MAP = new HashMap<>();

    @PostConstruct
    public void init() {
        long startTime = System.currentTimeMillis();
        log.info("开始加载敏感词库...");
        
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(SENSITIVE_WORD_PATTERN);
            
            if (resources.length == 0) {
                log.warn("未找到敏感词文件，请检查路径: {}", SENSITIVE_WORD_PATTERN);
                return;
            }
            
            log.info("找到 {} 个敏感词文件", resources.length);
            
            // 并行加载所有文件
            Arrays.stream(resources)
                    .parallel()
                    .forEach(this::loadWordsFromResource);
            
            long endTime = System.currentTimeMillis();
            log.info("敏感词库加载完成！共加载 {} 个敏感词，耗时 {} ms", 
                    WORD_COUNT.get(), endTime - startTime);
            
        } catch (Exception e) {
            log.error("加载敏感词文件失败", e);
        }
    }

    /**
     * 从资源文件加载敏感词
     */
    private void loadWordsFromResource(Resource resource) {
        int count = 0;
        String filename = resource.getFilename();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                String keyword = processLine(line);
                if (keyword != null && !keyword.isEmpty()) {
                    addKeyword(keyword, filename);
                    count++;
                }
            }
            log.debug("从文件 {} 加载了 {} 个敏感词", filename, count);
            WORD_COUNT.addAndGet(count);
            
        } catch (Exception e) {
            log.error("加载敏感词文件失败: {}", filename, e);
        }
    }

    /**
     * 处理每一行，过滤空行
     */
    private String processLine(String line) {
        if (line == null) {
            return null;
        }
        
        // 去除首尾空格
        line = line.trim();
        
        // 跳过空行
        if (line.isEmpty()) {
            return null;
        }
        
        return line;
    }

    /**
     * 将一个敏感词添加到前缀树中（线程安全）
     *
     * @param keyword 敏感词
     * @param sourceFile 来源文件名
     */
    private synchronized void addKeyword(String keyword, String sourceFile) {
        if (keyword == null || keyword.isEmpty()) {
            return;
        }
        
        // 记录敏感词与来源文件的映射关系
        WORD_SOURCE_MAP.put(keyword, sourceFile);
        
        TrieNode tempNode = ROOT_NODE;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNode(c);
            if (subNode == null) {
                // 初始化子节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }
            // 指向子节点,进入下一轮循环
            tempNode = subNode;
            // 设置结束标识
            if (i == keyword.length() - 1) {
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 检测文本是否包含敏感词
     *
     * @param text 待检测的文本
     * @return true-包含敏感词，false-不包含
     */
    public static boolean containsSensitiveWords(String text) {
        // 空文本直接返回false
        if (StringUtil.isBlank(text)) {
            return false;
        }
        // 指针1 - 当前Trie节点
        TrieNode tempNode = ROOT_NODE;
        // 指针2 - 检测起始位置
        int begin = 0;
        // 指针3 - 当前检测位置
        int position = 0;
        while (position < text.length()) {
            char c = text.charAt(position);
            // 跳过符号
            if (isSymbol(c)) {
                // 如果在根节点，移动起始位置
                if (tempNode == ROOT_NODE) {
                    begin++;
                }
                position++;
                continue;
            }
            // 检查下级节点
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                // 不是敏感词，从下一个位置重新开始检测
                position = ++begin;
                tempNode = ROOT_NODE;
            } else if (tempNode.isKeywordEnd()) {
                // 发现完整的敏感词，立即返回true
                return true;
            } else {
                // 继续检查下一个字符
                position++;
            }
        }
        return false;
    }

    /**
     * 查找文本中的所有敏感词
     *
     * @param text 待检测的文本
     * @return 敏感词列表
     */
    public static List<String> findSensitiveWords(String text) {
        List<String> sensitiveWords = new ArrayList<>();
        if (StringUtil.isBlank(text)) {
            return sensitiveWords;
        }
        
        TrieNode tempNode = ROOT_NODE;
        int begin = 0;
        int position = 0;
        
        while (position < text.length()) {
            char c = text.charAt(position);
            
            if (isSymbol(c)) {
                if (tempNode == ROOT_NODE) {
                    begin++;
                }
                position++;
                continue;
            }
            
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                position = ++begin;
                tempNode = ROOT_NODE;
            } else if (tempNode.isKeywordEnd()) {
                // 找到敏感词
                String word = text.substring(begin, position + 1);
                sensitiveWords.add(word);
                // 继续查找下一个
                position = ++begin;
                tempNode = ROOT_NODE;
            } else {
                position++;
            }
        }
        return sensitiveWords;
    }

    /**
     * 查找文本中的所有敏感词及其来源文件
     *
     * @param text 待检测的文本
     * @return 敏感词与来源文件的映射
     */
    public static Map<String, String> findSensitiveWordsWithSource(String text) {
        Map<String, String> result = new LinkedHashMap<>();
        if (StringUtil.isBlank(text)) {
            return result;
        }
        
        TrieNode tempNode = ROOT_NODE;
        int begin = 0;
        int position = 0;
        
        while (position < text.length()) {
            char c = text.charAt(position);
            
            if (isSymbol(c)) {
                if (tempNode == ROOT_NODE) {
                    begin++;
                }
                position++;
                continue;
            }
            
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                position = ++begin;
                tempNode = ROOT_NODE;
            } else if (tempNode.isKeywordEnd()) {
                // 找到敏感词
                String word = text.substring(begin, position + 1);
                String sourceFile = WORD_SOURCE_MAP.getOrDefault(word, "未知来源");
                result.put(word, sourceFile);
                // 继续查找下一个
                position = ++begin;
                tempNode = ROOT_NODE;
            } else {
                position++;
            }
        }
        return result;
    }

    /**
     * 获取敏感词的来源文件
     *
     * @param word 敏感词
     * @return 来源文件名
     */
    public static String getWordSource(String word) {
        return WORD_SOURCE_MAP.getOrDefault(word, "未知来源");
    }

    /**
     * 替换文本中的敏感词
     *
     * @param text        待过滤的文本
     * @param replacement 替换字符（如 *）
     * @return 过滤后的文本
     */
    public static String replaceSensitiveWords(String text, String replacement) {
        if (StringUtil.isBlank(text)) {
            return text;
        }
        
        StringBuilder result = new StringBuilder();
        TrieNode tempNode = ROOT_NODE;
        int begin = 0;
        int position = 0;
        
        while (position < text.length()) {
            char c = text.charAt(position);
            
            if (isSymbol(c)) {
                if (tempNode == ROOT_NODE) {
                    result.append(c);
                    begin++;
                }
                position++;
                continue;
            }
            
            tempNode = tempNode.getSubNode(c);
            if (tempNode == null) {
                result.append(text.charAt(begin));
                position = ++begin;
                tempNode = ROOT_NODE;
            } else if (tempNode.isKeywordEnd()) {
                // 替换敏感词
                int length = position - begin + 1;
                result.append(replacement.repeat(length));
                position++;
                begin = position;
                tempNode = ROOT_NODE;
            } else {
                position++;
            }
        }
        
        // 添加剩余字符
        result.append(text.substring(begin));
        return result.toString();
    }

    /**
     * 获取已加载的敏感词数量
     */
    public static int getWordCount() {
        return WORD_COUNT.get();
    }

    /**
     * 判断是否为符号
     *
     * @param c 字符
     * @return 判断
     */
    private static boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    public static boolean isAsciiAlpha(char ch) {
        return isAsciiAlphaUpper(ch) || isAsciiAlphaLower(ch);
    }

    public static boolean isAsciiAlphaUpper(char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    public static boolean isAsciiAlphaLower(char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    public static boolean isAsciiNumeric(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public static boolean isAsciiAlphanumeric(char ch) {
        return isAsciiAlpha(ch) || isAsciiNumeric(ch);
    }

    /**
     * 前缀树
     */
    private static class TrieNode {

        // 关键词结束标识
        private boolean isKeywordEnd = false;

        // 子节点
        private final Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        // 添加子节点
        public void addSubNode(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        // 获取子节点
        public TrieNode getSubNode(Character c) {
            return subNodes.get(c);
        }

    }
}

