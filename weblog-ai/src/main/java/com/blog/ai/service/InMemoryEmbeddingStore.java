package com.blog.ai.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 简单的内存向量存储，支持 JSON 文件持久化。
 * 不依赖任何外部向量数据库。
 */
@Data
@NoArgsConstructor
public class InMemoryEmbeddingStore {

    private List<Entry> entries = new ArrayList<>();

    public void add(String text, float[] embedding, Long articleId, String articleTitle) {
        Entry entry = new Entry();
        entry.setId(UUID.randomUUID().toString());
        entry.setText(text);
        entry.setEmbedding(embedding);
        entry.setArticleId(articleId);
        entry.setArticleTitle(articleTitle);
        entries.add(entry);
    }

    /**
     * 物理删除指定文章的所有向量条目。
     * 直接移除而非标记删除，避免 transient 字段在 JSON 持久化/重启后失效。
     */
    public void removeByArticleId(Long articleId) {
        entries.removeIf(e -> articleId.equals(e.getArticleId()));
    }

    /**
     * 检索最相似的文本块，返回结果包含相似度分数。
     */
    public List<SearchResultEntry> search(float[] queryEmbedding, int topK) {
        return entries.stream()
                .map(e -> {
                    double similarity = cosineSimilarity(queryEmbedding, e.getEmbedding());
                    return new SearchResultEntry(e, similarity);
                })
                .sorted((a, b) -> Double.compare(b.score, a.score))
                .limit(topK)
                .collect(Collectors.toList());
    }

    public void clear() {
        entries.clear();
    }

    public int entryCount() {
        return entries.size();
    }

    /**
     * 余弦相似度计算。对零向量做除零保护，避免 NaN 污染排序结果。
     */
    private double cosineSimilarity(float[] a, float[] b) {
        double dotProduct = 0, normA = 0, normB = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entry {
        private String id;
        private String text;
        private float[] embedding;
        private Long articleId;
        private String articleTitle;
    }

    /**
     * 搜索结果，包含文本块和相似度分数。
     */
    @Data
    @AllArgsConstructor
    public static class SearchResultEntry {
        private Entry entry;
        private double score;

        public String getText() { return entry.getText(); }
        public Long getArticleId() { return entry.getArticleId(); }
        public String getArticleTitle() { return entry.getArticleTitle(); }
    }
}
