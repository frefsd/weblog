package com.blog.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DingTalkNotifier {

    private final DingTalkProperties dingTalkProperties;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 发送 Markdown 消息到钉钉群
     */
    public boolean sendMarkdown(String title, String text) {
        String webhook = dingTalkProperties.getWebhook();
        if (webhook == null || webhook.isBlank()) {
            log.warn("钉钉 Webhook 未配置，跳过通知");
            return false;
        }

        try {
            Map<String, Object> markdown = Map.of(
                    "title", title,
                    "text", text
            );
            Map<String, Object> body = Map.of(
                    "msgtype", "markdown",
                    "markdown", markdown
            );
            restTemplate.postForEntity(webhook, body, String.class);
            log.info("钉钉通知发送成功: {}", title);
            return true;
        } catch (Exception e) {
            log.error("钉钉通知发送失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 发送告警消息
     */
    public boolean sendAlert(String ruleName, String logLevel, int triggerCount, int threshold, String errorMessage) {
        String title = "【WeBlog 告警】" + ruleName;
        String text = "### WeBlog 服务告警\n\n" +
                "- **服务名称**：weblog-server\n" +
                "- **告警规则**：" + ruleName + "\n" +
                "- **触发时间**：" + java.time.LocalDateTime.now() + "\n" +
                "- **日志级别**：" + logLevel + "\n" +
                "- **触发数量**：" + triggerCount + " 条（阈值 " + threshold + " 条）\n" +
                "- **最近错误**：`" + (errorMessage != null ? errorMessage : "无") + "`\n\n" +
                "---\n请及时登录后台查看详情。";
        return sendMarkdown(title, text);
    }
}
