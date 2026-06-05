package com.blog.ai.listener;

import com.blog.ai.event.ArticleChangeEvent;
import com.blog.ai.service.RagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleIndexListener {

    private final RagService ragService;

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
