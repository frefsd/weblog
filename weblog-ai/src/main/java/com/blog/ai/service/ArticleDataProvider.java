package com.blog.ai.service;

import com.blog.ai.dto.ArticleDocument;

import java.util.List;

/**
 * 文章数据提供者接口。
 *
 * <p>由 {@code weblog-server} 模块注入实现（通过 ArticleMapper + ArticleContentMapper 查询），
 * {@code weblog-ai} 模块通过此接口获取文章数据，避免直接依赖 weblog-server 的 Mapper。</p>
 */
@FunctionalInterface
public interface ArticleDataProvider {

    /**
     * 获取所有已发布文章（含正文内容）。
     *
     * @return 文章数据列表
     */
    List<ArticleDocument> loadAllPublishedArticles();
}
