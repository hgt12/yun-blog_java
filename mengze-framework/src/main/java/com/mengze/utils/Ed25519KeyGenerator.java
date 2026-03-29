package com.mengze.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * Ed25519密钥对生成工具
 * 用于生成和风天气JWT认证所需的密钥对
 */
public class Ed25519KeyGenerator
{

    /**
     * 生成Ed25519密钥对并保存到文件
     * 
     * @param privateKeyPath 私钥保存路径
     * @param publicKeyPath 公钥保存路径
     * @throws Exception 生成失败时抛出异常
     */
    public static void generateAndSaveKeys(String privateKeyPath, String publicKeyPath) throws Exception {
        // 生成密钥对
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("Ed25519");
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();

        // 保存私钥
        savePrivateKey(privateKey, privateKeyPath);
        
        // 保存公钥
        savePublicKey(publicKey, publicKeyPath);
        
        System.out.println("密钥对生成成功！");
        System.out.println("私钥保存在: " + privateKeyPath);
        System.out.println("公钥保存在: " + publicKeyPath);
        System.out.println("\n请将公钥内容上传到和风天气控制台：");
        System.out.println("=".repeat(50));
        printPublicKey(publicKey);
        System.out.println("=".repeat(50));
    }

    /**
     * 保存私钥到PEM文件
     */
    private static void savePrivateKey(PrivateKey privateKey, String filePath) throws IOException {
        String encoded = Base64.getEncoder().encodeToString(privateKey.getEncoded());
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("-----BEGIN PRIVATE KEY-----\n");
            writer.write(formatBase64(encoded));
            writer.write("\n-----END PRIVATE KEY-----\n");
        }
    }

    /**
     * 保存公钥到PEM文件
     */
    private static void savePublicKey(PublicKey publicKey, String filePath) throws IOException {
        String encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("-----BEGIN PUBLIC KEY-----\n");
            writer.write(formatBase64(encoded));
            writer.write("\n-----END PUBLIC KEY-----\n");
        }
    }

    /**
     * 打印公钥内容（用于上传到控制台）
     */
    private static void printPublicKey(PublicKey publicKey) {
        String encoded = Base64.getEncoder().encodeToString(publicKey.getEncoded());
        System.out.println("-----BEGIN PUBLIC KEY-----");
        System.out.println(formatBase64(encoded));
        System.out.println("-----END PUBLIC KEY-----");
    }

    /**
     * 格式化Base64字符串（每64个字符换行）
     */
    private static String formatBase64(String base64) {
        StringBuilder formatted = new StringBuilder();
        int index = 0;
        while (index < base64.length()) {
            int end = Math.min(index + 64, base64.length());
            formatted.append(base64, index, end);
            if (end < base64.length()) {
                formatted.append("\n");
            }
            index = end;
        }
        return formatted.toString();
    }

    /**
     * 主方法：运行此方法生成密钥对
     */
    public static void main(String[] args) {
        try {
            String privateKeyPath = "ed25519-private.pem";
            String publicKeyPath = "ed25519-public.pem";
            
            generateAndSaveKeys(privateKeyPath, publicKeyPath);
            
        } catch (Exception e) {
            System.err.println("生成密钥对失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
