package com.blog.ai.service.impl;

import com.blog.ai.config.AiProperties;
import com.blog.ai.entity.AiChatMemory;
import com.blog.ai.service.ChatMemoryService;
import com.blog.ai.service.ChatService;
import com.blog.ai.service.RagService;
import com.blog.ai.vo.ChatSourceVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    /**
     * 系统指令（纯规则描述，不含占位符）。
     * 发送给 LLM 时作为 system 角色的消息。
     */
    private static final String SYSTEM_INSTRUCTION = """
            你是 WeBlog 博客系统的智能助手，名字叫"小智"。

            你的任务是帮助用户解答技术问题，基于博客内容提供准确、有用的回答。

            ## 规则
            1. 回答要简洁、准确，优先使用博客内容中的信息
            2. 如果博客内容中没有相关信息，请说"根据现有博客内容无法回答这个问题"
            3. 不要编造博客中没有的信息
            4. 回答中适当使用表情符号，保持友好
            """;

    /**
     * 用户消息模板（包含对话历史、RAG 检索结果和当前问题）。
     */
    private static final String USER_PROMPT_TEMPLATE = """
            ## 对话历史
            %s

            ## 博客相关内容
            %s

            ## 用户问题
            %s

            ## 你的回答
            """;

    private final ChatMemoryService chatMemoryService;
    private final RagService ragService;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final RestClient.Builder restClientBuilder;

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

    /**
     * 解析会话 ID：如果前端已传入则沿用，否则创建新会话。
     * 不再擅自替换 sessionId，保持前后端一致，
     * 避免元数据中 sessionId 变化导致前端清空消息。
     */
    private String resolveSessionId(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return sessionId;
    }

    @Override
    public boolean validateSession(String sessionId) {
        return chatMemoryService.validateSession(sessionId);
    }

    @Override
    public List<Map<String, Object>> getChatHistory(String sessionId) {
        List<AiChatMemory> records = chatMemoryService.getChatHistory(sessionId);
        List<Map<String, Object>> messages = new ArrayList<>();
        for (AiChatMemory record : records) {
            Map<String, Object> userMsg = new HashMap<>();
            userMsg.put("isUser", true);
            userMsg.put("content", record.getUserMessage());
            messages.add(userMsg);

            Map<String, Object> aiMsg = new HashMap<>();
            aiMsg.put("isUser", false);
            aiMsg.put("content", record.getAiMessage());
            try {
                if (record.getSources() != null && !record.getSources().isBlank()) {
                    aiMsg.put("sources", objectMapper.readValue(record.getSources(), List.class));
                } else {
                    aiMsg.put("sources", List.of());
                }
            } catch (Exception e) {
                aiMsg.put("sources", List.of());
            }
            messages.add(aiMsg);
        }
        return messages;
    }

    @Override
    public StreamingResponseBody chatStreamText(String sessionId, String question) {
        String sid = resolveSessionId(sessionId);
        List<RagService.SearchResult> searchResults = ragService.search(question, aiProperties.getRag().getTopK());

        return outputStream -> {
            String prompt = buildPrompt(sid, question, searchResults);
            StringBuilder fullAnswer = new StringBuilder();
            log.info("纯文本流式开始，sessionId={}", sid);

            callChatApiStream(prompt, new StreamCallback() {
                @Override
                public void onToken(String token) {
                    fullAnswer.append(token);
                    try {
                        outputStream.write(token.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                    } catch (IOException e) {
                        // 客户端断开连接，停止流式
                        throw new RuntimeException("Client disconnected", e);
                    }
                }

                @Override
                public void onComplete() {
                    List<ChatSourceVO> sources = buildSources(searchResults);

                    // 先保存数据库（即使 meta 写入失败数据也不丢失）
                    try {
                        String sourcesJson = objectMapper.writeValueAsString(sources);
                        chatMemoryService.saveChat(sid, question, fullAnswer.toString(), sourcesJson);
                    } catch (Exception e) {
                        log.error("保存聊天记录失败(不影响使用): sessionId={}", sid, e);
                    }

                    // 再写元数据到响应（如果响应已关闭/超时，这里会抛 IOException 但数据已持久化）
                    Map<String, Object> meta = new HashMap<>();
                    meta.put("sessionId", sid);
                    try {
                        meta.put("sources", sources);
                        String metaJson = "\n___META___\n" + objectMapper.writeValueAsString(meta);
                        outputStream.write(metaJson.getBytes(StandardCharsets.UTF_8));
                        outputStream.flush();
                        log.info("纯文本流式完成，共 {} 字符", fullAnswer.length());
                    } catch (IOException e) {
                        log.warn("元数据写入响应失败(响应可能已关闭/超时), sessionId={}, 但数据已保存", sid);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    log.error("流式处理出错", error);
                }
            });
        };
    }

    /**
     * 组装用户 prompt（对话历史 + RAG 上下文 + 问题）。
     * 调用方负责先执行 ragService.search() 获取 searchResults。
     */
    private String buildPrompt(String sessionId, String question, List<RagService.SearchResult> searchResults) {
        var history = chatMemoryService.getRecentChats(sessionId, aiProperties.getChatMemory().getMaxRounds());
        StringBuilder historyText = new StringBuilder();
        for (var record : history) {
            historyText.append("用户：").append(record.getUserMessage()).append("\n");
            historyText.append("助手：").append(record.getAiMessage()).append("\n");
        }

        StringBuilder contextText = new StringBuilder();
        for (RagService.SearchResult result : searchResults) {
            contextText.append("- ").append(result.getText()).append("\n");
        }
        if (searchResults.isEmpty()) {
            contextText.append("（未找到相关博客内容）");
        }

        return String.format(USER_PROMPT_TEMPLATE, historyText.toString(), contextText.toString(), question);
    }

    /**
     * 流式调用智谱 AI Chat API（SSE）。
     * 使用 exchange() 绕过消息转换器，SSE 读取逻辑全部在回调内完成，
     * 避免 exchange() 返回后响应流被关闭的问题。
     */
    private void callChatApiStream(String prompt, StreamCallback callback) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", aiProperties.getZhipu().getModelName());
            requestBody.put("stream", true);
            requestBody.put("temperature", 0.7);

            ArrayNode messages = requestBody.putArray("messages");
            ObjectNode systemMsg = messages.addObject();
            systemMsg.put("role", "system");
            systemMsg.put("content", SYSTEM_INSTRUCTION);
            ObjectNode userMsg = messages.addObject();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);

            final int[] tokenCount = {0};
            restClient.post()
                    .uri("/chat/completions")
                    .body(requestBody.toString())
                    .exchange((request, response) -> {
                        try (BufferedReader reader = new BufferedReader(
                                new InputStreamReader(response.getBody(), "UTF-8"))) {

                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("data: ")) {
                                    String data = line.substring(6).trim();
                                    if ("[DONE]".equals(data)) {
                                        log.info("流式调用完成，共收到 {} 个 token", tokenCount[0]);
                                        callback.onComplete();
                                        return null;
                                    }
                                    try {
                                        JsonNode json = objectMapper.readTree(data);
                                        JsonNode delta = json.get("choices").get(0).get("delta");
                                        if (delta != null && delta.get("content") != null) {
                                            tokenCount[0]++;
                                            callback.onToken(delta.get("content").asText());
                                        }
                                    } catch (Exception e) {
                                        log.warn("解析SSE消息失败: {}", line);
                                    }
                                }
                            }
                            log.info("流式调用完成（非DONE结束），共收到 {} 个 token", tokenCount[0]);
                            callback.onComplete();
                        } catch (Exception e) {
                            callback.onError(e);
                        }
                        return null;
                    });
        } catch (Exception e) {
            callback.onError(e);
        }
    }

    private List<ChatSourceVO> buildSources(List<RagService.SearchResult> results) {
        List<ChatSourceVO> sources = new ArrayList<>();
        for (RagService.SearchResult result : results) {
            String snippet = result.getText();
            if (snippet.length() > 100) {
                snippet = snippet.substring(0, 100) + "...";
            }
            sources.add(new ChatSourceVO(result.getArticleId(), result.getArticleTitle(), snippet));
        }
        return sources;
    }

    private interface StreamCallback {
        void onToken(String token);
        void onComplete();
        void onError(Throwable error);
    }
}
