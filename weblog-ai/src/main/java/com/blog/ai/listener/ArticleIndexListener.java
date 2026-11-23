package com.blog.ai.listener;

import com.blog.ai.event.ArticleChangeEvent;
import com.blog.ai.service.RagService;
import com.blog.event.SeoRebuildRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleIndexListener {

    private final RagService ragService;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 听完事务提交后异步重建索引。
     *
     * <p>必须 {@code @Async}：索引构建包含逐块 embedding HTTP 调用与全量落盘，
     * 之前同步执行会把"发布文章"接口阻塞 5~20 秒，且失败的异常会传染给 Controller，
     * 造成"数据已入库但前端提示发布失败"，用户重试即产生重复文章。</p>
     */
    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleArticleChange(ArticleChangeEvent event) {
        log.info("收到文章变更事件: articleId={}, type={}", event.getArticleId(), event.getType());

        // 先请求前端重建：SEO 与 RAG 索引是两件独立的事，
        // embedding 接口超时不应导致文章永远没有预渲染页面
        eventPublisher.publishEvent(new SeoRebuildRequestedEvent(
                "文章变更: " + event.getType() + " id=" + event.getArticleId()));

        try {
            switch (event.getType()) {
                case CREATED -> ragService.addDocument(event.getArticleId(), event.getTitle(), event.getContent());
                case UPDATED -> ragService.updateDocument(event.getArticleId(), event.getTitle(), event.getContent());
                case DELETED -> ragService.removeDocument(event.getArticleId());
            }
        } catch (Exception e) {
            // 索引失败只影响 AI 问答效果，不能影响已经提交的文章数据
            log.error("文章索引更新失败: articleId={}, type={}", event.getArticleId(), event.getType(), e);
        }
    }
}
