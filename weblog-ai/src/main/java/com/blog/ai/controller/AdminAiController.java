package com.blog.ai.controller;

import com.blog.ai.dto.ArticleDocument;
import com.blog.ai.service.ArticleDataProvider;
import com.blog.ai.service.RagService;
import com.blog.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 管理接口控制器。
 *
 * <p>提供向量索引重建等管理功能，需要 ADMIN 角色。</p>
 */
@Tag(name = "AI 管理接口")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai/admin")
public class AdminAiController {

    private final ArticleDataProvider articleDataProvider;
    private final RagService ragService;

    /**
     * 全量重建向量索引。
     *
     * <p>从数据库加载所有已发布文章，重新分块向量化后存入向量库。</p>
     */
    @Operation(summary = "重建向量索引")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/rebuild")
    public Result<String> rebuildIndex() {
        List<ArticleDocument> articles = articleDataProvider.loadAllPublishedArticles();
        ragService.rebuildIndex(articles);
        return Result.ok("向量索引重建完成，共处理 " + articles.size() + " 篇文章");
    }
}
