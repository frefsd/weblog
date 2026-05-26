package com.blog;

import com.blog.entity.LogRecord;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 日志事件，由监控切面产生后通过 Spring 事件机制发布，
 * 由 {@link LogEventListener} 异步消费：持久化日志 + 触发告警评估。
 */
@Getter
public class LogEvent extends ApplicationEvent {
    private final LogRecord logRecord;

    public LogEvent(Object source, LogRecord logRecord) {
        super(source);
        this.logRecord = logRecord;
    }
}
