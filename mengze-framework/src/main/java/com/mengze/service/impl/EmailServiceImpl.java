package com.mengze.service.impl;

import com.mengze.constants.SystemConstants;
import com.mengze.domain.entity.Article;
import com.mengze.domain.entity.Comment;
import com.mengze.mapper.ArticleMapper;
import com.mengze.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

//邮件服务类
@Service
public class EmailServiceImpl
{
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${blog.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    //发送登录验证码邮件
    public void sendVerificationCodeByLogin(String to, String code)
    {
        sendVerificationEmail(to, code, "登录", "您正在进行登录操作，请使用以下验证码完成登录。");
    }
    
    //发送注册验证码邮件
    public void sendVerificationCodeByRegister(String to, String code)
    {
        sendVerificationEmail(to, code, "注册", "欢迎注册云梦泽的个人博客！请使用以下验证码完成注册。");
    }
    
    //发送忘记密码验证码邮件
    public void sendVerificationCodeByForgotPassword(String to, String code)
    {
        sendVerificationEmail(to, code, "重置密码", "您正在进行密码重置操作，请使用以下验证码完成密码重置。");
    }
    
//    通用发送验证码邮件方法
//    to 收件人邮箱
//    code 验证码
//    scene 场景（登录/注册/重置密码）
//    description 描述文字
    private void sendVerificationEmail(String to, String code, String scene, String description)
    {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("【云梦泽的个人博客】" + scene + "验证码");
            
            // HTML 邮件内容
            String htmlContent = buildEmailHtml(code, scene, description);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("邮件发送失败", e);
        }
    }

    //构建邮件HTML内容
    //code 验证码
    //scene 场景（登录/注册/重置密码）
    //description 描述文字
    private String buildEmailHtml(String code, String scene, String description)
    {
        // 根据场景选择不同的颜色主题
        String themeColor = getThemeColor(scene);
        String greeting = getGreeting(scene);
        
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "</head>" +
                "<body style=\"margin: 0; padding: 0; background: linear-gradient(135deg, #a5dff9 0%, #008c9e 100%); font-family: 'Segoe UI', Arial, sans-serif;\">" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 40px 20px;\">" +
                "        <tr>" +
                "            <td align=\"center\">" +
                "                <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f5fffa; border-radius: 12px; box-shadow: 0 12px 15px 0 rgba(0,0,0,0.24), 0 17px 50px 0 rgba(0,0,0,0.19); overflow: hidden;\">" +
                "                    <!-- 头部 -->" +
                "                    <tr>" +
                "                        <td style=\"background: rgb(125, 182, 191); padding: 40px 30px; text-align: center;\">" +
                "                            <h1 style=\"color: #2c3e50; margin: 0; font-size: 28px; font-weight: 600; letter-spacing: 1px;\">云梦泽的个人博客</h1>" +
                "                            <p style=\"color: #546e7a; margin: 10px 0 0 0; font-size: 14px;\">Cloud Dream Marsh Blog</p>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- 内容 -->" +
                "                    <tr>" +
                "                        <td style=\"padding: 50px 40px;\">" +
                "                            <h2 style=\"color: #3c4858; margin: 0 0 20px 0; font-size: 22px; font-weight: 600;\">" + greeting + "</h2>" +
                "                            <p style=\"color: #718096; line-height: 1.8; margin: 0 0 30px 0; font-size: 15px;\">" +
                "                                " + description +
                "                            </p>" +
                "                            <!-- 验证码区域 -->" +
                "                            <div style=\"background: linear-gradient(135deg, #f8f9fa 0%, #e8f4f8 100%); border-left: 4px solid " + themeColor + "; border-radius: 8px; padding: 30px; margin: 30px 0; text-align: center; box-shadow: 0 2px 8px rgba(48,169,222,0.1);\">" +
                "                                <p style=\"color: #718096; margin: 0 0 15px 0; font-size: 13px; text-transform: uppercase; letter-spacing: 1px;\">您的" + scene + "验证码</p>" +
                "                                <p style=\"color: " + themeColor + "; font-size: 36px; font-weight: bold; letter-spacing: 10px; margin: 0; font-family: 'Courier New', monospace;\">" + code + "</p>" +
                "                            </div>" +
                "                            <!-- 提示信息 -->" +
                "                            <div style=\"background-color: #fff8e1; border-left: 4px solid #ffc107; border-radius: 8px; padding: 20px; margin: 25px 0;\">" +
                "                                <p style=\"color: #3c4858; line-height: 1.8; margin: 0; font-size: 14px;\">" +
                "                                    验证码有效期为 <strong>5分钟</strong>，请尽快完成验证<br>" +
                "                                    请勿将验证码泄露给他人<br>" +
                "                                    如非本人操作，请忽略此邮件" +
                "                                </p>" +
                "                            </div>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- 底部 -->" +
                "                    <tr>" +
                "                        <td style=\"background: linear-gradient(135deg, #f8f9fa 0%, #eaecef 100%); padding: 30px; text-align: center; border-top: 1px solid #eaecef;\">" +
                "                            <p style=\"color: #718096; margin: 0 0 10px 0; font-size: 13px;\">" +
                "                                此邮件由系统自动发送，请勿直接回复" +
                "                            </p>" +
                "                            <p style=\"color: #a7a9ad; margin: 0; font-size: 12px;\">" +
                "                                © 2026 云梦泽的个人博客 · All rights reserved" +
                "                            </p>" +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +
                "                <!-- 额外提示 -->" +
                "                <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"margin-top: 20px;\">" +
                "                    <tr>" +
                "                        <td style=\"text-align: center; padding: 10px;\">" +
                "                            <p style=\"color: rgba(255,255,255,0.9); margin: 0; font-size: 12px; text-shadow: 0 1px 2px rgba(0,0,0,0.1);\">" +
                "                                如有疑问，请访问我们的网站或联系客服" +
                "                            </p>" +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +
                "            </td>" +
                "        </tr>" +
                "    </table>" +
                "</body>" +
                "</html>";
    }
    
    //根据场景获取主题颜色
    private String getThemeColor(String scene)
    {
        switch (scene) {
            case "登录":
                return "#30a9de";  // 蓝色
            case "注册":
                return "#52c41a";  // 绿色
            case "重置密码":
                return "#ff6b6b";  // 红色
            default:
                return "#30a9de";
        }
    }
    
    //根据场景获取问候语
    private String getGreeting(String scene)
    {
        switch (scene) {
            case "注册":
                return "欢迎加入！";
            case "重置密码":
                return "密码重置";
            default:
                return "您好！";
        }
    }

    //处理评论邮件通知（统一入口）
    public void sendCommentNotificationByEmail(Comment comment)
    {
        try {
            String articleTitle = getArticleTitle(comment);
            String articleUrl = buildArticleUrl(comment);

            // 判断是否为回复评论
            if (comment.getRootId() != null && comment.getRootId() != -1L)
            {
                // 这是一条回复，需要通知被回复的人
                Comment parentComment = commentMapper.selectById(comment.getReplyToCommentId());
                if (parentComment != null && StringUtils.hasText(parentComment.getEmail()))
                {
                    // 不要给自己发邮件（如果回复者和被回复者是同一个人）
                    if (!comment.getEmail().equalsIgnoreCase(parentComment.getEmail()))
                    {
                        sendCommentReplyNotificationByEmail(
                                parentComment.getEmail(),
                                articleTitle,
                                comment.getNickname(),
                                comment.getContent(),
                                parentComment.getContent(),
                                articleUrl
                        );
                    }
                }
            }

            // 如果不是博主自己评论，通知博主有新评论
            if (!comment.getEmail().equalsIgnoreCase(SystemConstants.BLOGGER_EMAIL))
            {
                sendNewCommentNotification(
                        articleTitle,
                        comment.getNickname(),
                        comment.getContent(),
                        articleUrl
                );
            }
        } catch (Exception e) {
            // 邮件发送失败不影响评论功能
            e.printStackTrace();
        }
    }

    //获取文章标题
    private String getArticleTitle(Comment comment)
    {
        String type = comment.getType();
        if (SystemConstants.COMMENT_TYPE_ARTICLE.equals(type) && comment.getArticleId() != null) {
            Article article = articleMapper.selectById(comment.getArticleId());
            return article != null ? article.getTitle() : "未知文章";
        } else if (SystemConstants.COMMENT_TYPE_LINK.equals(type)) {
            return "友链评论";
        } else if (SystemConstants.COMMENT_TYPE_MESSAGE.equals(type)) {
            return "留言板";
        }
        return "评论";
    }

    //构建文章URL
    private String buildArticleUrl(Comment comment)
    {
        String type = comment.getType();
        if (SystemConstants.COMMENT_TYPE_ARTICLE.equals(type) && comment.getArticleId() != null) {
            return frontendUrl + "/post/" + comment.getArticleId();
        } else if (SystemConstants.COMMENT_TYPE_LINK.equals(type)) {
            return frontendUrl + "/links";
        } else if (SystemConstants.COMMENT_TYPE_MESSAGE.equals(type)) {
            return frontendUrl + "/messages";
        }
        return frontendUrl;
    }

    //发送新评论通知邮件（有人评论了你的文章）
    public void sendNewCommentNotification(String articleTitle, String commenterNickname, String commentContent, String articleUrl)
    {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(from);
            helper.setTo(from); // 发送给博主自己
            helper.setSubject("【云梦泽的个人博客】您收到了新评论");
            
            String htmlContent = buildCommentNotificationHtml(articleTitle, commenterNickname, commentContent, articleUrl, "新评论");
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            // 邮件发送失败不影响评论功能，只记录日志
            e.printStackTrace();
        }
    }

    //发送评论回复通知邮件（有人回复了你的评论）
    public void sendCommentReplyNotificationByEmail(String toEmail, String articleTitle, String replierNickname, String replyContent, String originalComment, String articleUrl)
    {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(from);
            helper.setTo(toEmail);
            helper.setSubject("【云梦泽的个人博客】您的评论收到了新回复");
            
            String htmlContent = buildReplyNotificationHtml(articleTitle, replierNickname, replyContent, originalComment, articleUrl);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            // 邮件发送失败不影响评论功能，只记录日志
            e.printStackTrace();
        }
    }

    //构建评论通知邮件HTML内容
    private String buildCommentNotificationHtml(String articleTitle, String commenterNickname, String commentContent, String articleUrl, String notificationType)
    {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "</head>" +
                "<body style=\"margin: 0; padding: 0; background: linear-gradient(135deg, #a5dff9 0%, #008c9e 100%); font-family: 'Segoe UI', Arial, sans-serif;\">" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 40px 20px;\">" +
                "        <tr>" +
                "            <td align=\"center\">" +
                "                <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f5fffa; border-radius: 12px; box-shadow: 0 12px 15px 0 rgba(0,0,0,0.24), 0 17px 50px 0 rgba(0,0,0,0.19); overflow: hidden;\">" +
                "                    <!-- 头部 -->" +
                "                    <tr>" +
                "                        <td style=\"background: rgb(125, 182, 191); padding: 40px 30px; text-align: center;\">" +
                "                            <h1 style=\"color: #2c3e50; margin: 0; font-size: 28px; font-weight: 600; letter-spacing: 1px;\">云梦泽的个人博客</h1>" +
                "                            <p style=\"color: #546e7a; margin: 10px 0 0 0; font-size: 14px;\">评论通知</p>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- 内容 -->" +
                "                    <tr>" +
                "                        <td style=\"padding: 50px 40px;\">" +
                "                            <h2 style=\"color: #3c4858; margin: 0 0 20px 0; font-size: 22px; font-weight: 600;\">您收到了" + notificationType + "</h2>" +
                "                            <p style=\"color: #718096; line-height: 1.8; margin: 0 0 30px 0; font-size: 15px;\">" +
                "                                <strong>" + commenterNickname + "</strong> 在文章《<strong>" + articleTitle + "</strong>》中发表了评论。" +
                "                            </p>" +
                "                            <!-- 评论内容区域 -->" +
                "                            <div style=\"background: linear-gradient(135deg, #f8f9fa 0%, #e8f4f8 100%); border-left: 4px solid #30a9de; border-radius: 8px; padding: 25px; margin: 30px 0; box-shadow: 0 2px 8px rgba(48,169,222,0.1);\">" +
                "                                <p style=\"color: #718096; margin: 0 0 10px 0; font-size: 13px; text-transform: uppercase; letter-spacing: 1px;\">评论内容</p>" +
                "                                <p style=\"color: #3c4858; font-size: 15px; line-height: 1.8; margin: 0; word-wrap: break-word;\">" + commentContent + "</p>" +
                "                            </div>" +
                "                            <!-- 查看按钮 -->" +
                "                            <div style=\"text-align: center; margin: 30px 0;\">" +
                "                                <a href=\"" + articleUrl + "\" style=\"display: inline-block; background: linear-gradient(135deg, #30a9de 0%, #2196f3 100%); color: #ffffff; text-decoration: none; padding: 14px 40px; border-radius: 25px; font-size: 15px; font-weight: 600; box-shadow: 0 4px 15px rgba(48,169,222,0.3); transition: all 0.3s;\">查看评论</a>" +
                "                            </div>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- 底部 -->" +
                "                    <tr>" +
                "                        <td style=\"background: linear-gradient(135deg, #f8f9fa 0%, #eaecef 100%); padding: 30px; text-align: center; border-top: 1px solid #eaecef;\">" +
                "                            <p style=\"color: #718096; margin: 0 0 10px 0; font-size: 13px;\">" +
                "                                此邮件由系统自动发送，请勿直接回复" +
                "                            </p>" +
                "                            <p style=\"color: #a7a9ad; margin: 0; font-size: 12px;\">" +
                "                                © 2026 云梦泽的个人博客 · All rights reserved" +
                "                            </p>" +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +
                "            </td>" +
                "        </tr>" +
                "    </table>" +
                "</body>" +
                "</html>";
    }

    //构建回复通知邮件HTML内容
    private String buildReplyNotificationHtml(String articleTitle, String replierNickname, String replyContent, String originalComment, String articleUrl)
    {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "</head>" +
                "<body style=\"margin: 0; padding: 0; background: linear-gradient(135deg, #a5dff9 0%, #008c9e 100%); font-family: 'Segoe UI', Arial, sans-serif;\">" +
                "    <table width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"padding: 40px 20px;\">" +
                "        <tr>" +
                "            <td align=\"center\">" +
                "                <table width=\"600\" cellpadding=\"0\" cellspacing=\"0\" style=\"background-color: #f5fffa; border-radius: 12px; box-shadow: 0 12px 15px 0 rgba(0,0,0,0.24), 0 17px 50px 0 rgba(0,0,0,0.19); overflow: hidden;\">" +
                "                    <!-- 头部 -->" +
                "                    <tr>" +
                "                        <td style=\"background: rgb(125, 182, 191); padding: 40px 30px; text-align: center;\">" +
                "                            <h1 style=\"color: #2c3e50; margin: 0; font-size: 28px; font-weight: 600; letter-spacing: 1px;\">云梦泽的个人博客</h1>" +
                "                            <p style=\"color: #546e7a; margin: 10px 0 0 0; font-size: 14px;\">回复通知</p>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- 内容 -->" +
                "                    <tr>" +
                "                        <td style=\"padding: 50px 40px;\">" +
                "                            <h2 style=\"color: #3c4858; margin: 0 0 20px 0; font-size: 22px; font-weight: 600;\">您的评论收到了新回复</h2>" +
                "                            <p style=\"color: #718096; line-height: 1.8; margin: 0 0 30px 0; font-size: 15px;\">" +
                "                                <strong>" + replierNickname + "</strong> 在文章《<strong>" + articleTitle + "</strong>》中回复了您的评论。" +
                "                            </p>" +
                "                            <!-- 原评论 -->" +
                "                            <div style=\"background: #f8f9fa; border-left: 4px solid #cbd5e0; border-radius: 8px; padding: 20px; margin: 20px 0;\">" +
                "                                <p style=\"color: #718096; margin: 0 0 10px 0; font-size: 12px;\">您的评论</p>" +
                "                                <p style=\"color: #718096; font-size: 14px; line-height: 1.6; margin: 0; word-wrap: break-word;\">" + originalComment + "</p>" +
                "                            </div>" +
                "                            <!-- 回复内容 -->" +
                "                            <div style=\"background: linear-gradient(135deg, #f8f9fa 0%, #e8f4f8 100%); border-left: 4px solid #52c41a; border-radius: 8px; padding: 25px; margin: 20px 0; box-shadow: 0 2px 8px rgba(82,196,26,0.1);\">" +
                "                                <p style=\"color: #718096; margin: 0 0 10px 0; font-size: 13px; text-transform: uppercase; letter-spacing: 1px;\">回复内容</p>" +
                "                                <p style=\"color: #3c4858; font-size: 15px; line-height: 1.8; margin: 0; word-wrap: break-word;\">" + replyContent + "</p>" +
                "                            </div>" +
                "                            <!-- 查看按钮 -->" +
                "                            <div style=\"text-align: center; margin: 30px 0;\">" +
                "                                <a href=\"" + articleUrl + "\" style=\"display: inline-block; background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%); color: #ffffff; text-decoration: none; padding: 14px 40px; border-radius: 25px; font-size: 15px; font-weight: 600; box-shadow: 0 4px 15px rgba(82,196,26,0.3); transition: all 0.3s;\">查看回复</a>" +
                "                            </div>" +
                "                        </td>" +
                "                    </tr>" +
                "                    <!-- 底部 -->" +
                "                    <tr>" +
                "                        <td style=\"background: linear-gradient(135deg, #f8f9fa 0%, #eaecef 100%); padding: 30px; text-align: center; border-top: 1px solid #eaecef;\">" +
                "                            <p style=\"color: #718096; margin: 0 0 10px 0; font-size: 13px;\">" +
                "                                此邮件由系统自动发送，请勿直接回复" +
                "                            </p>" +
                "                            <p style=\"color: #a7a9ad; margin: 0; font-size: 12px;\">" +
                "                                © 2026 云梦泽的个人博客 · All rights reserved" +
                "                            </p>" +
                "                        </td>" +
                "                    </tr>" +
                "                </table>" +
                "            </td>" +
                "        </tr>" +
                "    </table>" +
                "</body>" +
                "</html>";
    }

}
