package com.blog;

import com.blog.entity.AlertRule;
import com.blog.service.AlertService;
import com.blog.service.MonitorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 日志事件监听器，异步消费 {@link LogEvent}：
 * 1. 将日志记录写入数据库
 * 2. 遍历启用的告警规则，仅对日志级别匹配的规则触发告警评估
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LogEventListener {

    private final MonitorLogService monitorLogService;
    private final AlertService alertService;

    @Async("monitorTaskExecutor")
    @EventListener
    public void handleLogEvent(LogEvent event) {
        try {
            // 1. 存储日志
            monitorLogService.save(event.getLogRecord());

            // 2. 评估告警规则（仅匹配规则配置的日志级别，无硬编码）
            List<AlertRule> rules = alertService.listEnabledRules();
            for (AlertRule rule : rules) {
                if (!rule.getLogLevel().equals(event.getLogRecord().getLevel())) {
                    continue;
                }
                try {
                    alertService.evaluateAndNotify(rule);
                } catch (Exception e) {
                    log.error("评估告警规则 {} 失败: {}", rule.getName(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("处理日志事件失败: {}", e.getMessage());
        }
    }
}
