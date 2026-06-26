package com.mengze.domain.dto;

import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 图形验证码校验请求DTO。
 *前端在完成验证码拖动/点击等交互后，将验证码ID和轨迹数据一并提交给后端，
 * 后端通过该DTO完成验证码校验与二次令牌签发
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "图形验证码校验请求对象")
public class ImageCaptchaCheckDto
{
    @NotBlank(message = "验证码ID不能为空")
    @ApiModelProperty(value = "验证码ID", required = true, example = "b6c4d2a1f8e0")
    private String id;

    @NotNull(message = "验证码轨迹数据不能为空")
    @ApiModelProperty(value = "验证码轨迹数据", required = true)
    private ImageCaptchaTrack data;
}
