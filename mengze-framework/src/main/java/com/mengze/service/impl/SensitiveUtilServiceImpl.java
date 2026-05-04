package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.github.houbb.sensitive.word.core.SensitiveWordHelper;
import com.mengze.constants.SystemConstants;
import com.mengze.enums.DetectionStatusEnum;
import com.mengze.service.AiChatService;
import com.mengze.service.SensitiveUtilService;
import com.mengze.utils.SensitiveUtil;
import com.mengze.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author mengze
 */
@Slf4j
@Service
public class SensitiveUtilServiceImpl implements SensitiveUtilService
{

    @Resource
    private AiChatService aiChatService;

    /**
     * 内容校验敏感词
     *
     * @param content 内容
     * @param email 评论者邮箱（用于判断是否为博主）
     * @return 是否敏感
     */
    @Override
    public Boolean checkSensitiveWords(String content, String email) {
        if (StringUtils.isEmpty(content)) {
            return false;
        }
        
        // 判断是否为博主，博主评论跳过敏感词检测
        if (SecurityUtils.isLogin() && SecurityUtils.isAdmin()) {
            log.info("博主评论（已登录），跳过敏感词检测");
            return false;
        }

        if (StringUtils.isNotEmpty(email) && email.equalsIgnoreCase(SystemConstants.BLOGGER_EMAIL)) {
            log.info("博主评论（未登录），跳过敏感词检测");
            return false;
        }
        
        //1.本地词库检测
        if (SensitiveUtil.containsSensitiveWords(content)) {
            // 获取检测到的具体敏感词及其来源文件
            java.util.Map<String, String> localSensitiveWordsWithSource = SensitiveUtil.findSensitiveWordsWithSource(content);
            log.info("本地词库检测为敏感词，检测到的敏感词及来源: {}", localSensitiveWordsWithSource);
            return true;
        }
        log.info("本地词库检测无敏感词 >>>>> 进入开源词库检测");
        //2.开源词库检测
        if (SensitiveWordHelper.contains(content)) {
            // 获取开源词库检测到的具体敏感词
            java.util.List<String> openSourceSensitiveWords = SensitiveWordHelper.findAll(content);
            log.info("开源词库检测为敏感词，检测到的敏感词: {}", openSourceSensitiveWords);
            return true;
        }
        log.info("开源词库检测无敏感词 >>>>> 进入多ai并行检测");
        //3.多AI并行检测
        DetectionStatusEnum aiStatus = aiChatService.aiCheckWord(content);
        if (aiStatus.shouldBlock()) {
            log.info("多AI并行检测为敏感词，内容: {}", content);
            return true;
        }
        log.info("多AI并行检测无敏感词 >>>>> 可以评论");
        return false;
    }

}
