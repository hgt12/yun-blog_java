package com.mengze.utils;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;

/**
 * 和风天气JWT工具类
 * 用于生成Ed25519算法签名的JWT Token
 */
public class QWeatherJwtUtil {

    /**
     * 生成和风天气API的JWT Token
     * 
     * @param keyId 凭据ID（在和风天气控制台获取）
     * @param projectId 项目ID（在和风天气控制台获取）
     * @param privateKeyPem 私钥内容（PEM格式）
     * @param validitySeconds Token有效期（秒），最长86400秒（24小时）
     * @return JWT Token
     * @throws Exception 生成失败时抛出异常
     */
    public static String generateToken(String keyId, String projectId, String privateKeyPem, long validitySeconds) throws Exception
    {
        // 清理私钥格式，处理字面的\n转义字符
        String privateKeyString = privateKeyPem
                .replace("\\n", "\n")  // 将字面的\n转换为真正的换行符
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        
        // 解码私钥
        byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyString);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        
        // 使用EdDSA算法生成私钥
        KeyFactory keyFactory = KeyFactory.getInstance("EdDSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // 构建Header
        String headerJson = String.format("{\"alg\": \"EdDSA\", \"kid\": \"%s\"}", keyId);

        // 构建Payload
        // iat提前30秒防止时间误差
        long iat = ZonedDateTime.now(ZoneOffset.UTC).toEpochSecond() - 30;
        long exp = iat + validitySeconds;
        String payloadJson = String.format("{\"sub\": \"%s\", \"iat\": %d, \"exp\": %d}", projectId, iat, exp);

        // Base64URL编码Header和Payload
        String headerEncoded = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadEncoded = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String data = headerEncoded + "." + payloadEncoded;

        // 调试输出
//        System.out.println("========== 和风天气JWT生成调试信息 ==========");
//        System.out.println("Header JSON: " + headerJson);
//        System.out.println("Payload JSON: " + payloadJson);
//        System.out.println("Header (Base64URL): " + headerEncoded);
//        System.out.println("Payload (Base64URL): " + payloadEncoded);
//        System.out.println("Data (header.payload): " + data);

        // 使用Ed25519签名
        Signature signer = Signature.getInstance("EdDSA");
        signer.initSign(privateKey);
        signer.update(data.getBytes(StandardCharsets.UTF_8));
        byte[] signature = signer.sign();

        // Base64URL编码签名
        String signatureEncoded = base64UrlEncode(signature);

//        System.out.println("Signature (Base64URL): " + signatureEncoded);

        // 拼接完整JWT: header.payload.signature
        String jwt = data + "." + signatureEncoded;
//        System.out.println("完整JWT: " + jwt);
//        System.out.println("JWT长度: " + jwt.length());
//        System.out.println("==========================================");
        
        return jwt;
    }

    /**
     * Base64URL编码
     */
    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}
