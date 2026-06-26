package com.mengze.domain.config;

import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.resource.ResourceStore;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import cloud.tianai.captcha.resource.impl.LocalMemoryResourceStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CaptchaResourceConfig
{

    /**
     * 配置验证码资源存储器
     * @return ResourceStore
     */
    @Bean
    public ResourceStore resourceStore() {
        // 使用简单的本地内存存储器，也可以使用数据库等存储
        LocalMemoryResourceStore resourceStore = new LocalMemoryResourceStore();

        // 配置背景图
        // arg1: 验证码类型(SLIDER、ROTATE、CONCAT、WORD_IMAGE_CLICK)
        // arg2: Resource对象，包含: 资源类型(calsspath、file、url)、文件路径、tag标签
        // 滑块型
        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "image/captchaImages/captchaImages-1.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "image/captchaImages/captchaImages-2.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "image/captchaImages/captchaImages-3.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "image/captchaImages/captchaImages-4.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "image/captchaImages/captchaImages-5.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "image/captchaImages/captchaImages-6.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "image/captchaImages/captchaImages-7.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "image/captchaImages/captchaImages-8.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.SLIDER, new Resource("classpath", "image/captchaImages/captchaImages-9.jpg", "default"));

        // 旋转型
        resourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", "image/captchaImages/captchaImages-1.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", "image/captchaImages/captchaImages-2.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", "image/captchaImages/captchaImages-3.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", "image/captchaImages/captchaImages-4.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", "image/captchaImages/captchaImages-5.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", "image/captchaImages/captchaImages-6.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", "image/captchaImages/captchaImages-7.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", "image/captchaImages/captchaImages-8.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.ROTATE, new Resource("classpath", "image/captchaImages/captchaImages-9.jpg", "default"));

        // 滑动裁切图型
        resourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", "image/captchaImages/captchaImages-1.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", "image/captchaImages/captchaImages-2.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", "image/captchaImages/captchaImages-3.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", "image/captchaImages/captchaImages-4.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", "image/captchaImages/captchaImages-5.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", "image/captchaImages/captchaImages-6.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", "image/captchaImages/captchaImages-7.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", "image/captchaImages/captchaImages-8.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.CONCAT, new Resource("classpath", "image/captchaImages/captchaImages-9.jpg", "default"));

        // 文字点击型
        resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "image/captchaImages/captchaImages-1.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "image/captchaImages/captchaImages-2.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "image/captchaImages/captchaImages-3.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "image/captchaImages/captchaImages-4.png", "default"));
        resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "image/captchaImages/captchaImages-5.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "image/captchaImages/captchaImages-6.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "image/captchaImages/captchaImages-7.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "image/captchaImages/captchaImages-8.jpg", "default"));
        resourceStore.addResource(CaptchaTypeConstant.WORD_IMAGE_CLICK, new Resource("classpath", "image/captchaImages/captchaImages-9.jpg", "default"));

        return resourceStore;
    }

}
