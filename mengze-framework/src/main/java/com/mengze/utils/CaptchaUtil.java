package com.mengze.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码工具类
 */
public class CaptchaUtil {

    private static final String CODES = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final Random RANDOM = new Random();

    /**
     * 生成验证码文本
     */
    public static String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CODES.charAt(RANDOM.nextInt(CODES.length())));
        }
        return code.toString();
    }

    /**
     * 生成验证码图片并转为Base64
     */
    public static String generateImageBase64(String code) throws IOException {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 32));

        // 绘制验证码
        for (int i = 0; i < code.length(); i++) {
            // 随机颜色
            g.setColor(new Color(RANDOM.nextInt(150), RANDOM.nextInt(150), RANDOM.nextInt(150)));
            // 随机位置和角度
            int x = 20 + i * 25;
            int y = 28 + RANDOM.nextInt(8);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }

        // 绘制干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(RANDOM.nextInt(200), RANDOM.nextInt(200), RANDOM.nextInt(200)));
            int x1 = RANDOM.nextInt(WIDTH);
            int y1 = RANDOM.nextInt(HEIGHT);
            int x2 = RANDOM.nextInt(WIDTH);
            int y2 = RANDOM.nextInt(HEIGHT);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制噪点
        for (int i = 0; i < 50; i++) {
            g.setColor(new Color(RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255)));
            g.fillRect(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 1, 1);
        }

        g.dispose();

        // 转换为Base64
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", outputStream);
        String base64 = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        return "data:image/png;base64," + base64;
    }
}
