package com.blog.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SEO 自动重建配置。
 *
 * <p>默认关闭：本地开发时文章一保存就触发构建会很烦，也避免在没装 Node/Chrome
 * 的环境里反复失败。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "seo.auto-rebuild")
public class SeoRebuildProperties {

    /** 是否启用文章变更后自动重建 */
    private boolean enabled = false;

    /**
     * 实际执行的命令，例如 {@code bash /www/deploy/weblog-rebuild.sh}。
     * 为空时不执行任何操作。
     */
    private String command = "";

    /** 命令的工作目录，为空则使用应用启动目录 */
    private String workingDir = "";

    /** 防抖窗口（秒）：窗口内多次文章变更合并为一次执行 */
    private int debounceSeconds = 60;

    /** 单次执行超时（秒），超时后强制终止子进程 */
    private int timeoutSeconds = 600;
}
