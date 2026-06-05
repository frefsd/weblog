package com.blog.listener;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.ai.dto.ArticleDocument;
import com.blog.ai.service.ArticleDataProvider;
import com.blog.entity.Article;
import com.blog.entity.ArticleContent;
import com.blog.mapper.ArticleContentMapper;
import com.blog.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link ArticleDataProvider} 的实现，注入 weblog-server 的 Mapper 查询文章数据。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleDataProviderImpl implements ArticleDataProvider {

    private final ArticleMapper articleMapper;
    private final ArticleContentMapper articleContentMapper;

    @Override
    public List<ArticleDocument> loadAllPublishedArticles() {
        List<Article> articles = articleMapper.selectList(
                new LambdaQueryWrapper<Article>().eq(Article::getIsDeleted, 0)
        );

        List<ArticleDocument> documents = articles.stream().map(article -> {
            ArticleContent content = articleContentMapper.selectOne(
                    new LambdaQueryWrapper<ArticleContent>().eq(ArticleContent::getArticleId, article.getId())
            );
            return new ArticleDocument(
                    article.getId(),
                    article.getTitle(),
                    content != null ? content.getContent() : ""
            );
        }).collect(Collectors.toList());

        log.info("加载已发布文章数据: {} 篇", documents.size());
        return documents;
    }
}
