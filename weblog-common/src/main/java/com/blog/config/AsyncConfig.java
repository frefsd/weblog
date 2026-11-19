package com.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务线程池配置。
 *
 * <p>所有线程池统一使用 {@link ThreadPoolExecutor.CallerRunsPolicy}：
 * 默认的 AbortPolicy 在队列打满时会把 {@code TaskRejectedException}
 * 抛回调用方线程——而监控切面、AI 索引监听器都在业务线程上发布事件，
 * 等于"系统最忙的时候监控反而把业务请求打挂"。CallerRuns 退化为同步执行，
 * 只拖慢、不失败。</p>
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {

    /**
     * 访客记录异步写入线程池。
     */
    @Bean("visitorTaskExecutor")
    public Executor visitorTaskExecutor() {
        return buildExecutor("visitor-task-", 2, 4, 200);
    }

    /**
     * 监控日志异步落库线程池。
     */
    @Bean("monitorTaskExecutor")
    public Executor monitorTaskExecutor() {
        return buildExecutor("monitor-task-", 2, 4, 500);
    }

    /**
     * AI 向量索引构建线程池。
     *
     * <p>索引构建包含逐块 embedding HTTP 调用，耗时可能达到数十秒，
     * 必须与请求线程隔离，否则发布文章接口会被同步阻塞。</p>
     */
    @Bean("aiTaskExecutor")
    public Executor aiTaskExecutor() {
        return buildExecutor("ai-task-", 1, 2, 500);
    }

    private Executor buildExecutor(String prefix, int corePoolSize, int maxPoolSize, int queueCapacity) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(prefix);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
