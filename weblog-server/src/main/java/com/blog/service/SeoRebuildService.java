package com.blog.service;

import com.blog.config.SeoRebuildProperties;
import com.blog.event.SeoRebuildRequestedEvent;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * SEO 自动重建服务。
 *
 * <p>文章发布/更新/删除后，前端需要重新构建才能生成新的预渲染页面，
 * 否则新文章在 sitemap 里存在、但页面仍是空壳。</p>
 *
 * <p><b>设计取舍：</b>后端只负责"什么时候该构建"，具体怎么构建（切目录、发布、回滚）
 * 全部交给部署方提供的脚本，避免把打包流程硬编码进业务代码。</p>
 *
 * <p><b>防抖：</b>连续发布多篇文章时，{@code debounceSeconds} 窗口内的请求会合并成一次。
 * 由于靠 {@code @Scheduled} 每 15 秒轮询，实际延迟是
 * {@code debounceSeconds} ~ {@code debounceSeconds + 15s}。</p>
 */
@Slf4j
@Service
public class SeoRebuildService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String LOG_FILE = "logs/seo-rebuild.log";
    /** 轮询间隔，与 scheduledTickMillis 保持一致 */
    private static final long TICK_MILLIS = 15_000L;

    private final SeoRebuildProperties properties;

    /** 构建命令可能跑很久，必须丢进独立线程池，不能阻塞调度线程 */
    private final Executor taskExecutor;

    /**
     * 最近一次重建请求的时间戳，0 表示当前没有待执行的请求。
     *
     * <p>用单个 AtomicLong 而不是 boolean + AtomicLong 两个变量：后者在
     * "读取 pending → 执行 → 置 false" 的窗口里，会把窗口内新到的请求覆盖掉。</p>
     */
    private final AtomicLong pendingSince = new AtomicLong(0);

    /** 是否正在执行，避免并发构建把产物写坏 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile String lastResult = "尚未执行";
    private volatile LocalDateTime lastRunAt;

    public SeoRebuildService(SeoRebuildProperties properties,
                             @Qualifier("seoTaskExecutor") Executor taskExecutor) {
        this.properties = properties;
        this.taskExecutor = taskExecutor;
    }

    @PostConstruct
    void logResolvedPaths() {
        log.info("SEO 自动重建：enabled={}, 命令={}, 日志={}",
                properties.isEnabled(),
                properties.getCommand() == null || properties.getCommand().isBlank() ? "(未配置)" : properties.getCommand(),
                new File(LOG_FILE).getAbsolutePath());
        if (properties.isEnabled()
                && (properties.getCommand() == null || properties.getCommand().isBlank())) {
            log.warn("已开启 SEO 自动重建但未配置 seo.auto-rebuild.command，文章变更不会触发构建");
        }
    }

    @EventListener
    public void onRebuildRequested(SeoRebuildRequestedEvent event) {
        schedule(event.reason());
    }

    /**
     * 提交一次重建请求（不会立即执行，由定时任务在防抖窗口结束后触发）。
     *
     * @return 便于管理端回显的结果描述
     */
    public String schedule(String reason) {
        if (!properties.isEnabled()) {
            return "未启用：seo.auto-rebuild.enabled = false";
        }
        if (properties.getCommand() == null || properties.getCommand().isBlank()) {
            return "未配置：seo.auto-rebuild.command 为空";
        }

        long previous = pendingSince.getAndSet(System.currentTimeMillis());
        if (previous == 0) {
            log.info("已排入 SEO 重建队列: {}", reason);
        } else {
            log.info("SEO 重建请求已合并（防抖窗口内）: {}", reason);
        }
        return "已提交，将在防抖窗口结束后执行";
    }

    /**
     * 每 15 秒检查一次：防抖窗口结束且有请求时，把构建任务交给独立线程池。
     */
    @Scheduled(fixedDelay = TICK_MILLIS, initialDelay = TICK_MILLIS)
    public void flushIfDue() {
        long requestedAt = pendingSince.get();
        if (requestedAt == 0) {
            return;
        }
        if (System.currentTimeMillis() - requestedAt < properties.getDebounceSeconds() * 1000L) {
            return;
        }
        if (!running.compareAndSet(false, true)) {
            return;
        }
        // CAS 清零：若这期间又来了新请求，compareAndSet 会失败，把机会留给下一轮，
        // 而不是无条件覆盖（否则那次文章变更就被静默吞掉了）
        if (!pendingSince.compareAndSet(requestedAt, 0)) {
            running.set(false);
            return;
        }

        try {
            taskExecutor.execute(() -> {
                try {
                    execute();
                } finally {
                    running.set(false);
                }
            });
        } catch (RuntimeException e) {
            running.set(false);
            // 提交失败必须把请求放回队列，否则这次变更永远等不到重建
            pendingSince.compareAndSet(0, System.currentTimeMillis());
            log.error("SEO 重建任务提交失败，已重新排队", e);
        }
    }

    private void execute() {
        String command = properties.getCommand().trim();
        List<String> argv = isWindows()
                ? List.of("cmd.exe", "/c", command)
                : List.of("sh", "-c", command);

        File workDir = properties.getWorkingDir() == null || properties.getWorkingDir().isBlank()
                ? null
                : new File(properties.getWorkingDir());
        File logFile = new File(LOG_FILE).getAbsoluteFile();

        log.info("开始执行 SEO 重建命令: {}（工作目录: {}）",
                command, workDir == null ? "默认" : workDir.getAbsolutePath());

        long start = System.currentTimeMillis();
        try {
            File logParent = logFile.getParentFile();
            if (logParent != null && !logParent.exists()) {
                logParent.mkdirs();
            }

            ProcessBuilder builder = new ProcessBuilder(argv);
            if (workDir != null) {
                builder.directory(workDir);
            }
            // 合并 stderr 并直接重定向到文件，避免管道缓冲区写满导致子进程阻塞
            builder.redirectErrorStream(true);
            builder.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));

            Process process = builder.start();
            boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                finish("超时（" + properties.getTimeoutSeconds() + "s），已强制终止");
                return;
            }

            int exitCode = process.exitValue();
            long cost = System.currentTimeMillis() - start;
            finish(exitCode == 0
                    ? "成功（" + cost + "ms）"
                    : "失败（退出码 " + exitCode + "，详见 " + logFile.getPath() + "）");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            finish("被中断");
        } catch (Exception e) {
            log.error("SEO 重建执行异常", e);
            finish("异常: " + e.getMessage());
        }
    }

    private void finish(String result) {
        this.lastRunAt = LocalDateTime.now();
        this.lastResult = result;
        if (result.startsWith("成功")) {
            log.info("SEO 重建完成: {}", result);
        } else {
            log.warn("SEO 重建结束: {}", result);
        }
    }

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    /**
     * 查询当前状态，供管理端确认配置是否生效。
     */
    public Map<String, Object> status() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("enabled", properties.isEnabled());
        data.put("commandConfigured", properties.getCommand() != null && !properties.getCommand().isBlank());
        data.put("debounceSeconds", properties.getDebounceSeconds());
        data.put("pending", pendingSince.get() != 0);
        data.put("running", running.get());
        data.put("lastRunAt", lastRunAt == null ? null : lastRunAt.format(TIME_FORMATTER));
        data.put("lastResult", lastResult);
        return data;
    }
}
