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
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG 索引服务实现。
 *
 * <p><b>并发说明（原实现的问题与修复）：</b>原先把 6 个方法全部 {@code synchronized}，
 * 导致 embedding HTTP 调用和全量 JSON 落盘期间，所有 AI 问答检索请求排队等待。
 * 现在改为：</p>
 * <ul>
 *   <li>底层存储使用 {@link java.util.concurrent.CopyOnWriteArrayList}，检索无锁；</li>
 *   <li>全量重建通过 {@code replaceAll} 原子切换，不阻塞查询；</li>
 *   <li>落盘使用快照 + 独立的 {@code persistLock}，不与检索共享锁；</li>
 *   <li>索引构建本身由 {@code aiTaskExecutor} 单线程串行执行，无需再加全局锁。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RagServiceImpl implements RagService {

    private final InMemoryEmbeddingStore embeddingStore;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;

    private static final String STORE_FILE = System.getProperty("user.dir") + "/data/embedding-store.json";

    /** 只用于串行化文件写入，避免多线程同时写同一个文件把 JSON 写坏 */
    private final Object persistLock = new Object();

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
    public void rebuildIndex(List<ArticleDocument> articles) {
        List<InMemoryEmbeddingStore.Entry> allEntries = new ArrayList<>();
        for (ArticleDocument doc : articles) {
            allEntries.addAll(buildEntries(doc.getId(), doc.getTitle(), doc.getContent()));
        }
        // 原子替换：重建过程中查询方仍然看到完整的旧索引
        embeddingStore.replaceAll(allEntries);
        persist();
        log.info("向量索引重建完成: {} 篇文章, {} 个文本块", articles.size(), allEntries.size());
    }

    @Override
    public void addDocument(Long articleId, String title, String content) {
        int chunks = indexDocument(articleId, title, content);
        persist();
        log.info("新增文章索引: articleId={}, chunks={}", articleId, chunks);
    }

    @Override
    public void updateDocument(Long articleId, String title, String content) {
        // 先把 embedding 全部算完（N 次外网 HTTP），再一次性原子替换。
        // 不能"先删旧条目、再逐块调接口添加"——那段时间内检索该文章会返回空结果。
        List<InMemoryEmbeddingStore.Entry> entries = buildEntries(articleId, title, content);
        embeddingStore.replaceByArticleId(articleId, entries);
        persist();
        log.info("更新文章索引: articleId={}, chunks={}", articleId, entries.size());
    }

    @Override
    public void removeDocument(Long articleId) {
        embeddingStore.removeByArticleId(articleId);
        persist();
        log.info("删除文章索引: articleId={}", articleId);
    }

    @Override
    public List<SearchResult> search(String query, int topK) {
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
    public void persist() {
        // 先在无锁状态下取快照，再串行写盘：写文件不再阻塞检索
        InMemoryEmbeddingStore snapshot = embeddingStore.snapshot();
        synchronized (persistLock) {
            File file = new File(STORE_FILE);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            // 先写临时文件再原子改名：进程被 kill 时不会留下半个 JSON，
            // 否则下次启动 AiConfig 读失败会静默丢弃整个索引
            File tmp = new File(STORE_FILE + ".tmp");
            try {
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(tmp, snapshot);
                try {
                    Files.move(tmp.toPath(), file.toPath(),
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException e) {
                    Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
                log.debug("向量存储已持久化: {} 条记录", snapshot.entryCount());
            } catch (IOException e) {
                log.error("向量存储持久化失败: {}", e.getMessage(), e);
                if (tmp.exists() && !tmp.delete()) {
                    log.warn("临时索引文件清理失败: {}", tmp.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 对一篇文章进行分块 + 向量化 + 存入 store（增量场景）。
     *
     * @return 成功入库的文本块数量
     */
    private int indexDocument(Long articleId, String title, String content) {
        List<InMemoryEmbeddingStore.Entry> entries = buildEntries(articleId, title, content);
        for (InMemoryEmbeddingStore.Entry entry : entries) {
            embeddingStore.add(entry.getText(), entry.getEmbedding(), entry.getArticleId(), entry.getArticleTitle());
        }
        return entries.size();
    }

    /**
     * 分块 + 向量化，只构造条目不写入存储（供全量重建批量替换使用）。
     */
    private List<InMemoryEmbeddingStore.Entry> buildEntries(Long articleId, String title, String content) {
        String fullText = title + "\n" + (content != null ? content : "");
        List<String> chunks = TextSplitter.split(fullText,
                aiProperties.getEmbedding().getChunkSize(),
                aiProperties.getEmbedding().getChunkOverlap());

        List<InMemoryEmbeddingStore.Entry> entries = new ArrayList<>(chunks.size());
        for (String chunk : chunks) {
            float[] embedding = embedSingle(chunk);
            if (embedding != null) {
                entries.add(InMemoryEmbeddingStore.buildEntry(chunk, embedding, articleId, title));
            }
        }
        return entries;
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
