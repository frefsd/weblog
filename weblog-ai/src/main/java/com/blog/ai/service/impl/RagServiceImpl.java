package com.blog.ai.service.impl;

import com.blog.ai.config.AiProperties;
import com.blog.ai.dto.ArticleDocument;
import com.blog.ai.service.InMemoryEmbeddingStore;
import com.blog.ai.service.RagService;
import com.blog.ai.util.TextSplitter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final InMemoryEmbeddingStore embeddingStore;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;

    private static final String STORE_FILE = System.getProperty("user.dir") + "/data/embedding-store.json";

    /** 复用的 RestClient 实例，避免每次 API 调用都新建连接 */
    private RestClient restClient;

    @PostConstruct
    private void initRestClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(aiProperties.getApi().getTimeoutSeconds() * 1000);
        factory.setReadTimeout(aiProperties.getApi().getTimeoutSeconds() * 1000);
        this.restClient = restClientBuilder
                .baseUrl(aiProperties.getZhipu().getBaseUrl())
                .defaultHeader("Authorization", "Bearer " + aiProperties.getZhipu().getApiKey())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(factory)
                .build();
    }

    @Override
    public synchronized void rebuildIndex(List<ArticleDocument> articles) {
        embeddingStore.clear();
        int totalChunks = 0;
        for (ArticleDocument doc : articles) {
            totalChunks += indexDocument(doc.getId(), doc.getTitle(), doc.getContent());
        }
        persist();
        log.info("向量索引重建完成: {} 篇文章, {} 个文本块", articles.size(), totalChunks);
    }

    @Override
    public synchronized void addDocument(Long articleId, String title, String content) {
        int chunks = indexDocument(articleId, title, content);
        persist();
        log.info("新增文章索引: articleId={}, chunks={}", articleId, chunks);
    }

    @Override
    public synchronized void updateDocument(Long articleId, String title, String content) {
        // 先删除旧条目，再添加新条目（removeByArticleId 物理删除，不依赖 transient 标记）
        embeddingStore.removeByArticleId(articleId);
        int chunks = indexDocument(articleId, title, content);
        persist();
        log.info("更新文章索引: articleId={}, chunks={}", articleId, chunks);
    }

    @Override
    public synchronized void removeDocument(Long articleId) {
        embeddingStore.removeByArticleId(articleId);
        persist();
        log.info("删除文章索引: articleId={}", articleId);
    }

    @Override
    public synchronized List<SearchResult> search(String query, int topK) {
        float[] queryEmbedding = embed(query);
        if (queryEmbedding == null) {
            log.warn("查询向量化失败: {}", query);
            return List.of();
        }
        List<InMemoryEmbeddingStore.SearchResultEntry> results = embeddingStore.search(queryEmbedding, topK);
        return results.stream().map(e -> {
            SearchResult sr = new SearchResult();
            sr.setText(e.getText());
            sr.setArticleId(e.getArticleId());
            sr.setArticleTitle(e.getArticleTitle());
            sr.setScore(e.getScore());
            return sr;
        }).collect(Collectors.toList());
    }

    @Override
    public synchronized void persist() {
        try {
            File file = new File(STORE_FILE);
            file.getParentFile().mkdirs();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, embeddingStore);
            log.debug("向量存储已持久化: {} 条记录", embeddingStore.entryCount());
        } catch (IOException e) {
            log.error("向量存储持久化失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 对一篇文章进行分块 + 向量化 + 存入 store
     * @return 文本块数量
     */
    private int indexDocument(Long articleId, String title, String content) {
        String fullText = title + "\n" + (content != null ? content : "");
        List<String> chunks = TextSplitter.split(fullText,
                aiProperties.getEmbedding().getChunkSize(),
                aiProperties.getEmbedding().getChunkOverlap());

        int successCount = 0;
        for (String chunk : chunks) {
            float[] embedding = embedSingle(chunk);
            if (embedding != null) {
                embeddingStore.add(chunk, embedding, articleId, title);
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * 调用智谱 AI embedding API 将文本向量化（复用已构建的 RestClient 实例）。
     */
    private float[] embedSingle(String text) {
        try {
            String requestBody = objectMapper.createObjectNode()
                    .put("model", aiProperties.getZhipu().getEmbeddingModel())
                    .put("input", text)
                    .toString();

            String response = restClient.post()
                    .uri("/embeddings")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode json = objectMapper.readTree(response);
            JsonNode dataArray = json.get("data");
            if (dataArray == null || !dataArray.isArray() || dataArray.isEmpty()) {
                log.error("Embedding API 返回格式异常: {}", response);
                return null;
            }
            JsonNode embeddingArray = dataArray.get(0).get("embedding");
            if (embeddingArray == null) {
                log.error("Embedding API 返回数据中缺少 embedding 字段");
                return null;
            }
            float[] result = new float[embeddingArray.size()];
            for (int i = 0; i < embeddingArray.size(); i++) {
                result[i] = (float) embeddingArray.get(i).asDouble();
            }
            return result;
        } catch (Exception e) {
            log.error("Embedding API 调用失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 调用智谱 AI embedding API（向后兼容，供外部直接调用）。
     */
    private float[] embed(String text) {
        return embedSingle(text);
    }
}
