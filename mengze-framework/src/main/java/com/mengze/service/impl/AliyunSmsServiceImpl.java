package com.mengze.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

//阿里云号码认证服务 - 短信验证码
//短信验证码服务类
@Service
public class AliyunSmsServiceImpl
{
    @Value("${aliyun.sms.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.sms.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.sms.sign-name}")
    private String signName;

    @Value("${aliyun.sms.template-code}")
    private String templateCode;

    //阿里云客户端
    private Client createClient() throws Exception
    {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret)
                .setEndpoint("dypnsapi.aliyuncs.com");
        return new Client(config);
    }

    //发送验证码短信
    public void sendVerificationCode(String phone, String code)
    {
        try {
            Client client = createClient();
            
            // 构建模板参数 JSON 字符串
            String templateParam = String.format("{\"code\":\"%s\",\"min\":\"5\"}", code);
            
            // 创建请求对象
            SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam(templateParam);
            
            // 创建运行时选项
            RuntimeOptions runtime = new RuntimeOptions();
            
            // 发送短信
            SendSmsVerifyCodeResponse response = client.sendSmsVerifyCodeWithOptions(request, runtime);
            
            // 检查发送结果
            if (response != null && response.getBody() != null) {
                String resultCode = response.getBody().getCode();
                if ("OK".equals(resultCode)) {
                    System.out.println("短信发送成功，RequestId: " + response.getBody().getRequestId());
                } else {
                    String errorMsg = response.getBody().getMessage();
                    throw new RuntimeException("短信发送失败: " + errorMsg);
                }
            } else {
                throw new RuntimeException("短信发送失败: 响应为空");
            }
            
        } catch (TeaException error) {
            // Tea异常处理
            System.err.println("短信发送失败: " + error.getMessage());
            if (error.getData() != null && error.getData().containsKey("Recommend")) {
                System.err.println("诊断地址: " + error.getData().get("Recommend"));
            }
            throw new RuntimeException("短信发送失败: " + error.getMessage(), error);
        } catch (Exception e) {
            // 其他异常处理
            throw new RuntimeException("短信发送失败: " + e.getMessage(), e);
        }
    }
}
