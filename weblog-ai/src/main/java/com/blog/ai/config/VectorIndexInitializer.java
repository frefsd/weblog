package com.blog.ai.config;

import com.blog.ai.service.ArticleDataProvider;
import com.blog.ai.service.InMemoryEmbeddingStore;
import com.blog.ai.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 向量索引初始化器。
 *
 * <p>应用启动完成后自动检测向量库状态：
 * 如果为空则自动触发全量重建，消
 * 如果已有数据则跳过，保留现有索引。</p>
 *
 * <p><b>效果：</b>无论本地开发还是宝塔部署，首次启动自动建索引，
 * 后续重启复用已有文件，无需手动调 /ai/admin/rebuild。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorIndexInitializer {

    private final InMemoryEmbeddingStore embeddingStore;
    private final ArticleDataProvider articleDataProvider;
    private final RagService ragService;

    /**
     * 启动后异步重建索引。
     *
     * <p>之前同步执行：首次部署（或 data 目录被清空）时，全量重建要串行调用
     * 数百次 embedding 接口，启动阶段长时间不可用。异步后启动立即完成，
     * 索引在后台补齐。</p>
     */
    @Async("aiTaskExecutor")
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        if (embeddingStore.entryCount() > 0) {
            log.info("向量索引已存在 ({} 条记录)，跳过自动重建", embeddingStore.entryCount());
            return;
        }

        log.info("向量索引为空，开始首次全量重建...");
        var articles = articleDataProvider.loadAllPublishedArticles();
        if (articles.isEmpty()) {
            log.info("数据库中没有文章，跳过重建");
            return;
        }

        ragService.rebuildIndex(articles);
        log.info("首次向量索引自动重建完成: {} 篇文章", articles.size());
    }
}
