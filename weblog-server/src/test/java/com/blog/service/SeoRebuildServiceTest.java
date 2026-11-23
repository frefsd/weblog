package com.blog.service;

import com.blog.config.SeoRebuildProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SEO 自动重建服务的单元测试。
 *
 * <p>覆盖：未启用、命令未配置、防抖窗口、窗口结束后执行、执行失败。</p>
 */
class SeoRebuildServiceTest {

    private SeoRebuildService service(boolean enabled, String command, int debounceSeconds) {
        SeoRebuildProperties properties = new SeoRebuildProperties();
        properties.setEnabled(enabled);
        properties.setCommand(command);
        properties.setDebounceSeconds(debounceSeconds);
        properties.setTimeoutSeconds(30);
        // 同步 Executor：让断言能在 flushIfDue 之后立即检查结果
        return new SeoRebuildService(properties, Runnable::run);
    }

    @Test
    void 未启用时不执行任何命令() {
        SeoRebuildService service = service(false, "echo should-not-run", 0);

        String message = service.schedule("测试");
        service.flushIfDue();

        assertTrue(message.contains("未启用"), "应提示未启用，实际: " + message);
        Map<String, Object> status = service.status();
        assertEquals("尚未执行", status.get("lastResult"));
        assertEquals(false, status.get("pending"));
    }

    @Test
    void 命令未配置时不执行() {
        SeoRebuildService service = service(true, "   ", 0);

        String message = service.schedule("测试");
        service.flushIfDue();

        assertTrue(message.contains("未配置"), "应提示未配置命令，实际: " + message);
        assertEquals("尚未执行", service.status().get("lastResult"));
        assertEquals(false, service.status().get("pending"));
    }

    @Test
    void 防抖窗口内不执行窗口结束后执行() throws Exception {
        SeoRebuildService service = service(true, "echo seo-rebuild-test", 1);

        service.schedule("测试");
        service.flushIfDue();

        // 窗口内（1 秒未到）不应执行
        assertEquals("尚未执行", service.status().get("lastResult"));
        assertEquals(true, service.status().get("pending"));

        Thread.sleep(1100L);
        service.flushIfDue();

        String result = String.valueOf(service.status().get("lastResult"));
        assertTrue(result.startsWith("成功"), "命令应执行成功，实际: " + result);
        assertEquals(false, service.status().get("pending"));
    }

    @Test
    void 命令失败时记录失败结果() {
        SeoRebuildService service = service(true, "this-command-does-not-exist-12345", 0);

        service.schedule("测试");
        service.flushIfDue();

        String result = String.valueOf(service.status().get("lastResult"));
        assertTrue(result.startsWith("失败"), "应记录失败，实际: " + result);
        assertNotEquals(Boolean.TRUE, service.status().get("running"));
    }
}
