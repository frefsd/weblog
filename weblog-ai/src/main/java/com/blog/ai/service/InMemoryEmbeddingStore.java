package com.blog.ai.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 简单的内存向量存储，支持 JSON 文件持久化。
 * 不依赖任何外部向量数据库。
 *
 * <p><b>并发设计：</b>底层使用 {@link CopyOnWriteArrayList}，读多写少场景下
 * 检索不再需要加锁，也不会因为重建索引而阻塞所有查询请求；
 * 全量重建通过 {@link #replaceAll(List)} 原子切换，避免"清空后逐条重建"期间
 * 检索到残缺索引。</p>
 */
@Data
@NoArgsConstructor
public class InMemoryEmbeddingStore {

    /**
     * volatile 保证 replaceAll / replaceByArticleId 的引用切换对检索线程立即可见，
     * 否则"原子切换"只停留在 CopyOnWriteArrayList 内部，JMM 层面不成立。
     */
    private volatile List<Entry> entries = new CopyOnWriteArrayList<>();

    /**
     * 覆盖 Lombok 生成的 setter：反序列化时把任意 List 包装成写时复制列表，
     * 否则 JSON 还原出来的是普通 ArrayList，并发检索会抛 ConcurrentModificationException。
     */
    public void setEntries(List<Entry> entries) {
        this.entries = entries == null
                ? new CopyOnWriteArrayList<>()
                : new CopyOnWriteArrayList<>(entries);
    }

    public void add(String text, float[] embedding, Long articleId, String articleTitle) {
        entries.add(buildEntry(text, embedding, articleId, articleTitle));
    }

    /**
     * 构造条目但不加入存储，供全量重建时先收集再一次性替换。
     */
    public static Entry buildEntry(String text, float[] embedding, Long articleId, String articleTitle) {
        Entry entry = new Entry();
        entry.setId(UUID.randomUUID().toString());
        entry.setText(text);
        entry.setEmbedding(embedding);
        entry.setArticleId(articleId);
        entry.setArticleTitle(articleTitle);
        return entry;
    }

    /**
     * 原子替换全部条目（一次引用赋值），重建期间旧索引依然可查。
     */
    public void replaceAll(List<Entry> newEntries) {
        this.entries = new CopyOnWriteArrayList<>(newEntries == null ? List.of() : newEntries);
    }

    /**
     * 原子替换某篇文章的全部条目（删除旧条目 + 写入新条目，一次引用切换）。
     *
     * <p>用于文章更新：若先删后加，两次操作之间检索该文章会返回空结果，
     * 而 embedding 计算耗时可达数秒，空档期会被用户感知到。</p>
     */
    public void replaceByArticleId(Long articleId, List<Entry> newEntries) {
        List<Entry> updated = new ArrayList<>(entries);
        updated.removeIf(e -> articleId.equals(e.getArticleId()));
        if (newEntries != null && !newEntries.isEmpty()) {
            updated.addAll(newEntries);
        }
        this.entries = new CopyOnWriteArrayList<>(updated);
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
     * 生成一份条目快照，用于"锁外序列化落盘"，避免写文件时阻塞检索或读到半成品。
     */
    public InMemoryEmbeddingStore snapshot() {
        InMemoryEmbeddingStore copy = new InMemoryEmbeddingStore();
        copy.entries = new CopyOnWriteArrayList<>(new ArrayList<>(entries));
        return copy;
    }

    /**
     * 余弦相似度计算。对零向量做除零保护，避免 NaN 污染排序结果。
     */
    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null) {
            return 0.0;
        }
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
