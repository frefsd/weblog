package com.blog.ai.service;

import com.blog.ai.dto.ArticleDocument;

import java.util.List;

public interface RagService {
    void rebuildIndex(List<ArticleDocument> articles);
    void addDocument(Long articleId, String title, String content);
    void updateDocument(Long articleId, String title, String content);
    void removeDocument(Long articleId);
    List<SearchResult> search(String query, int topK);
    void persist();

    class SearchResult {
        private String text;
        private Long articleId;
        private String articleTitle;
        private double score;

        public SearchResult() {}

        public SearchResult(String text, Long articleId, String articleTitle, double score) {
            this.text = text;
            this.articleId = articleId;
            this.articleTitle = articleTitle;
            this.score = score;
        }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
        public Long getArticleId() { return articleId; }
        public void setArticleId(Long articleId) { this.articleId = articleId; }
        public String getArticleTitle() { return articleTitle; }
        public void setArticleTitle(String articleTitle) { this.articleTitle = articleTitle; }
        public double getScore() { return score; }
        public void setScore(double score) { this.score = score; }
    }
}
