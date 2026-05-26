package com.blog.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailNotifier {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String from;

    @Value("${alert.email:}")
    private String to;

    /**
     * 发送告警邮件
     */
    public boolean sendAlert(String ruleName, String logLevel, int triggerCount, int threshold, String errorMessage) {
        if (to == null || to.isBlank()) {
            log.warn("告警邮箱未配置（alert.email），跳过邮件通知");
            return false;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("[WeBlog 告警] " + ruleName);
            message.setText("服务名称：weblog-server\n"
                    + "告警规则：" + ruleName + "\n"
                    + "触发时间：" + java.time.LocalDateTime.now() + "\n"
                    + "日志级别：" + logLevel + "\n"
                    + "触发数量：" + triggerCount + " 条（阈值 " + threshold + " 条）\n"
                    + "最近错误：" + (errorMessage != null ? errorMessage : "无") + "\n\n"
                    + "请及时登录后台查看详情。");
            mailSender.send(message);
            log.info("告警邮件发送成功: {}", ruleName);
            return true;
        } catch (Exception e) {
            log.error("告警邮件发送失败: {}", e.getMessage());
            return false;
        }
    }
}
