package com.blog.aspect;

import com.blog.entity.LogRecord;
import com.blog.LogEvent;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 控制器请求监控切面，拦截 com.blog.controller 包下所有方法调用，
 * 记录请求耗时、URI、HTTP 方法等信息，慢请求（>3秒）和异常请求发布 {@link LogEvent} 事件。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ControllerMonitorAspect {

    private final ApplicationEventPublisher eventPublisher;
    private final HttpServletRequest request;

    @Pointcut("execution(* com.blog.controller..*.*(..))")
    public void controllerPointcut() {
    }

    @Around("controllerPointcut()")
    public Object monitorController(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        LogRecord record = new LogRecord();
        record.setServiceName("weblog-server");
        record.setMethod(methodName);
        record.setType("HTTP_REQUEST");
        record.setUri(request.getRequestURI());
        record.setHttpMethod(request.getMethod());

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - start;
            record.setDuration(duration);

            // 慢请求（超过 3 秒）记录 WARN 并发布事件
            if (duration > 3000) {
                record.setLevel("WARN");
                record.setErrorMessage("响应超时，耗时: " + duration + "ms");
                record.setIp(getClientIp());
                eventPublisher.publishEvent(new LogEvent(this, record));
            }

            return result;
        } catch (Throwable e) {
            long duration = System.currentTimeMillis() - start;

            record.setLevel("ERROR");
            record.setDuration(duration);
            record.setErrorMessage(e.getMessage());
            record.setStackTrace(getStackTrace(e));
            record.setIp(getClientIp());

            eventPublisher.publishEvent(new LogEvent(this, record));

            throw e;
        }
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        return ip;
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
