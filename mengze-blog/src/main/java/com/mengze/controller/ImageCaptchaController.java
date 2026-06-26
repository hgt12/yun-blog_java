package com.mengze.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import com.mengze.annotation.SystemLog;
import com.mengze.domain.dto.ImageCaptchaCheckDto;
import com.mengze.domain.vo.CategoryVo;
import com.mengze.service.CaptchaVerificationService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

// 图形验证码接口
@RestController
public class ImageCaptchaController
{
    @Autowired
    private ImageCaptchaApplication application;

    @Autowired
    private CaptchaVerificationService captchaVerificationService;

    /**
     * 生成验证码
     * @return 验证码数据
     */
    @PostMapping("/genCaptcha")
    @SystemLog(businessName = "生成图形验证码接口")
    @ApiOperation(value = "生成图形验证码接口", notes = "生成验证码(该数据返回给前端用于展示验证码数据)", response = ImageCaptchaVO.class)
    public ApiResponse<ImageCaptchaVO> genCaptcha()
    {
        // 1.生成验证码(该数据返回给前端用于展示验证码数据)
        // 参数1为具体的验证码类型， 默认支持 SLIDER（滑块）、ROTATE（旋转）、WORD_IMAGE_CLICK（文字点击）、CONCAT（滑动裁切图型） 等验证码类型，详见： `CaptchaTypeConstant`类
        return  application.generateCaptcha(CaptchaTypeConstant.SLIDER);
    }

    /**
     * 校验验证码（前端只传回轨迹数据，所有分析逻辑在后端执行）
     * 校验通过后会颁发一个二次验证 Token，用于后续敏感操作（如发送验证码）的身份核验。
     * Token 一次性使用，有效期 2 分钟。
     * @param data 验证码数据（id + 前端采集的轨迹数据）
     * @return 校验结果，成功时包含二次验证 Token
     */
    @PostMapping("/check")
    @ResponseBody
    @SystemLog(businessName = "校验验证码")
    @ApiOperation(value = "校验验证码", notes = "校验通过后会颁发一个二次验证 Token，用于后续敏感操作（如发送验证码）的身份核验。")
    public ApiResponse<?> checkCaptcha(@RequestBody ImageCaptchaCheckDto data)
    {
        // 第一步：一次验证 — 后端分析轨迹数据，判断是否为人操作
        ApiResponse<?> response = application.matching(data.getId(), data.getData());
        if (response.isSuccess()) {
            // 第二步：二次验证 — 颁发临时 Token，供后续敏感操作使用
            String token = captchaVerificationService.generateToken(data.getId());

            Map<String, String> result = new HashMap<>();
            result.put("id", data.getId());
            result.put("token", token);
            return ApiResponse.ofSuccess(result);
        }
        return response;
    }

}
