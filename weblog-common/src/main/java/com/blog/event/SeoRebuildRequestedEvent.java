package com.blog.event;

/**
 * 请求重建前端静态站点（预渲染）的事件。
 *
 * <p>放在 common 模块，让 weblog-ai（文章变更后发布）与 weblog-server（执行构建）
 * 都能引用，而不产生模块间循环依赖。</p>
 *
 * @param reason 触发原因，仅用于日志排查
 */
public record SeoRebuildRequestedEvent(String reason) {
}
