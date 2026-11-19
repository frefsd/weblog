package com.blog.ai.listener;

import com.blog.ai.event.ArticleChangeEvent;
import com.blog.ai.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleIndexListener {

    private final RagService ragService;

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
        switch (event.getType()) {
            case CREATED -> ragService.addDocument(event.getArticleId(), event.getTitle(), event.getContent());
            case UPDATED -> ragService.updateDocument(event.getArticleId(), event.getTitle(), event.getContent());
            case DELETED -> ragService.removeDocument(event.getArticleId());
        }
    }
}
