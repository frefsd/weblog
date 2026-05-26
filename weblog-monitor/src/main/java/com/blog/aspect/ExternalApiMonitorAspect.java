package com.blog.aspect;

import com.blog.MonitorExternal;
import com.blog.entity.LogRecord;
import com.blog.LogEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 外部 API 调用监控切面，拦截标记了 {@link MonitorExternal} 注解的方法，
 * 记录调用耗时，超时（>10秒）或异常时发布 {@link LogEvent} 事件。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ExternalApiMonitorAspect {

    private final ApplicationEventPublisher eventPublisher;

    @Pointcut("@annotation(com.blog.MonitorExternal)")
    public void externalApiPointcut() {
    }

    @Around("externalApiPointcut() && @annotation(annotation)")
    public Object monitorExternalApi(ProceedingJoinPoint joinPoint, MonitorExternal annotation) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        LogRecord record = new LogRecord();
        record.setServiceName("weblog-server");
        record.setMethod(methodName);
        record.setType("EXTERNAL_API");

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;

            record.setLevel("INFO");
            record.setDuration(duration);

            if (duration > 10000) {
                record.setLevel("WARN");
                record.setErrorMessage("外部 API(" + annotation.service() + ") 调用超时，耗时: " + duration + "ms");
                eventPublisher.publishEvent(new LogEvent(this, record));
            }

            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;

            record.setLevel("ERROR");
            record.setDuration(duration);
            record.setErrorMessage("外部 API(" + annotation.service() + ") 调用失败: " + e.getMessage());
            record.setStackTrace(getStackTrace(e));

            eventPublisher.publishEvent(new LogEvent(this, record));

            throw e;
        }
    }

    private String getStackTrace(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e).append("\n");
        StackTraceElement[] elements = e.getStackTrace();
        int limit = Math.min(elements.length, 20);
        for (int i = 0; i < limit; i++) {
            sb.append("\tat ").append(elements[i]).append("\n");
        }
        return sb.toString();
    }
}
