package com.mengze.service.impl;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mengze.enums.DetectionStatusEnum;
import com.mengze.domain.dto.ModelConfigDto;
import com.mengze.service.AiChatService;
import com.mengze.utils.AiPromptTemplateUtils;
import io.github.pigmesh.ai.deepseek.core.DeepSeekClient;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionRequest;
import io.github.pigmesh.ai.deepseek.core.chat.ChatCompletionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author mengze
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService
{

    @Resource
    private DeepSeekClient deepSeekClient;

    //需要调用的大大模型
    private static final List<ModelConfigDto> MODEL_CONFIGS = Collections.unmodifiableList(Arrays.asList(
            new ModelConfigDto("deepseek-v4-pro", 10, 1.0f),
            new ModelConfigDto("deepseek-v4-flash", 8, 0.9f)
    ));

    private static final ExecutorService MODEL_EXECUTOR = Executors.newCachedThreadPool(
            new ThreadFactoryBuilder().setNameFormat("ai-detection-%d").setDaemon(true).build()
    );


    /**
     * AI检测敏感词
     * @param word 待检测内容
     * @return DetectionStatus 检测状态
     */
    @Override
    public DetectionStatusEnum aiCheckWord(String word) {
        if (StringUtils.isEmpty(word)) {
            return DetectionStatusEnum.CLEAR;
        }
        // 短内容使用单模型快速检测
        if (word.length() < 5) {
            return checkWithSingleModel(word, MODEL_CONFIGS.get(1));
        }
        // 中长内容使用多模型检测
        return checkWithOptimizedModels(word);
    }

    private DetectionStatusEnum checkWithOptimizedModels(String content) {
        List<ModelConfigDto> sortedModels = MODEL_CONFIGS.stream()
                .sorted(Comparator.comparingInt(ModelConfigDto::getPriority).reversed())
                .collect(Collectors.toList());
        // 创建并行任务列表
        List<CompletableFuture<DetectionStatusEnum>> futures = sortedModels.stream()
                .map(config -> CompletableFuture.supplyAsync(
                        () -> checkWithSingleModel(content, config),
                        MODEL_EXECUTOR
                ).exceptionally(e -> {
                    log.warn("模型检测异常: {}", e.getMessage());
                    return DetectionStatusEnum.SUGGEST_CLOUD_CHECK;
                })).collect(Collectors.toList());
        // 使用allOf等待所有任务完成（无论成功与否）
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );
        try {
            // 设置总超时时间
            allFutures.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            log.warn("部分模型检测未能在5秒内完成，继续处理已完成结果");
        } catch (Exception e) {
            log.error("多模型检测发生异常", e);
        }
        // 收集所有结果（包括已完成和异常的）
        List<DetectionStatusEnum> results = futures.stream()
                .map(future -> {
                    try {
                        // 未完成则返回默认
                        return future.getNow(DetectionStatusEnum.SUGGEST_CLOUD_CHECK);
                    } catch (Exception e) {
                        return DetectionStatusEnum.SUGGEST_CLOUD_CHECK;
                    }
                }).collect(Collectors.toList());

        // 综合评估策略
        return evaluateConsensusResults(results);
    }

    /**
     * 综合评估策略（升级版）
     */
    private DetectionStatusEnum evaluateConsensusResults(List<DetectionStatusEnum> results) {
        if (results.isEmpty()) {
            return DetectionStatusEnum.SUGGEST_CLOUD_CHECK;
        }
        // 统计各状态数量
        Map<DetectionStatusEnum, Long> countMap = results.stream()
                .collect(Collectors.groupingBy(
                        status -> status,
                        Collectors.counting()
                ));
        long sensitiveCount = countMap.getOrDefault(DetectionStatusEnum.SENSITIVE, 0L);
        long clearCount = countMap.getOrDefault(DetectionStatusEnum.CLEAR, 0L);
        long totalModels = results.size();
        // 策略1：超过半数模型明确判定敏感
        if (sensitiveCount > totalModels / 2) {
            log.info("AI多模型判定结果为：超过半数模型明确判定敏感");
            return DetectionStatusEnum.SENSITIVE;
        }
        // 策略2：超过70%模型明确判定非敏感
        if (clearCount > totalModels * 0.7) {
            log.info("AI多模型判定结果为：超过70%模型明确判定非敏感");
            return DetectionStatusEnum.CLEAR;
        }
        // 策略3：存在敏感判定且总占比超过30%
        if (sensitiveCount > 0 && (sensitiveCount + clearCount) * 0.3 < sensitiveCount) {
            log.info("AI多模型判定结果为：存在敏感判定且总占比超过30%");
            return DetectionStatusEnum.SENSITIVE;
        }
        // 其他情况建议云校验
        return DetectionStatusEnum.SUGGEST_CLOUD_CHECK;
    }

    private DetectionStatusEnum checkWithSingleModel(String content, ModelConfigDto config) {
        try {
            long startTime = System.currentTimeMillis();
            String prompt = AiPromptTemplateUtils.buildSensitiveCheckPrompt(content);
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .addUserMessage(prompt)
                    .model(config.getName())
                    .stream(false)
                    .temperature(0.0)
                    .maxCompletionTokens(30)
                    .reasoningEffort("low") // 表示低推理强度，不是深度思考模式
                    .build();
            // 带超时的请求
            CompletableFuture<ChatCompletionResponse> future = CompletableFuture.supplyAsync(
                    () -> deepSeekClient.chatCompletion(request).execute()
            );
            ChatCompletionResponse response = future.get(10000, TimeUnit.MILLISECONDS);
            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                log.warn("模型 {} 返回空响应", config.getName());
                return DetectionStatusEnum.SUGGEST_CLOUD_CHECK;
            }
            String answer = response.choices().get(0).message().content().trim()
                    .replace("\uFEFF", "")
                    .replaceAll("^```json|```$", "");
            try {
                JsonNode json = new ObjectMapper()
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                        .readTree(answer);
                boolean sensitive = json.path("sensitive").asBoolean();
                float confidence = (float) json.path("confidence").asDouble(0.5);
                log.info("模型 {} 检测结果: {}, 置信度: {}, 检测耗时: {}ms", config.getName(), sensitive, confidence, System.currentTimeMillis() - startTime);
                // 根据置信度确定最终状态
                if (sensitive) {
                    return confidence >= 0.8 ? DetectionStatusEnum.SENSITIVE : DetectionStatusEnum.SUGGEST_CLOUD_CHECK;
                } else {
                    return confidence >= 0.7 ? DetectionStatusEnum.CLEAR : DetectionStatusEnum.SUGGEST_CLOUD_CHECK;
                }
            } catch (Exception e) {
                log.warn("解析模型 {} 响应失败: {}", config.getName(), answer);
                return DetectionStatusEnum.SUGGEST_CLOUD_CHECK;
            }
        } catch (TimeoutException e) {
            log.warn("模型 {} 检测超时", config.getName());
            return DetectionStatusEnum.SUGGEST_CLOUD_CHECK;
        } catch (Exception e) {
            log.error("模型 {} 检测异常: {}", config.getName(), e.getMessage());
            return DetectionStatusEnum.SUGGEST_CLOUD_CHECK;
        }
    }
}
