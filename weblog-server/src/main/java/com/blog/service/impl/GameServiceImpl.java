package com.blog.service.impl;

import com.blog.service.IGameService;
import com.blog.vo.GameReplyVO;
import com.blog.vo.GameStartVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 游戏服务实现
 *
 * 核心设计：后端权威计分，LLM 只负责生成对话内容。
 * 后端解析 LLM 输出中的分数变更，校验后更新原谅值。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements IGameService {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.model-name:deepseek-v4-flash}")
    private String modelName;

    /** 内存中管理游戏会话 */
    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

    /** System prompt：含评分规则和严格输出格式 */
    private static final String SYSTEM_PROMPT = """
            你扮演用户女友的角色。现在你很生气，用户需要尽可能说正确的话来哄你开心。

            规则：
            - 初始原谅值为 20，每次交互会增加或减少原谅值
            - 原谅值达到 100 游戏通关，原谅值为 0 则游戏失败
            - 每次用户回复分为 5 个等级：
              -10 为非常生气，-5 为生气，0 为正常，+5 为开心，+10 为非常开心

            输出格式（严格按此格式输出，不要有多余内容）：
            女友说的话
            得分：+/-N
            原谅值：当前值/100

            注意：你只能以女友身份回答，不是以AI身份或用户身份。
            """;

    // 正则：匹配得分行，如 得分：+5 或 得分：-10
    private static final Pattern SCORE_PATTERN = Pattern.compile("得分[：:]\\s*([+-]?\\d+)");

    @Override
    public GameStartVO startGame() {
        String sessionId = UUID.randomUUID().toString();

        GameSession session = new GameSession();
        session.setSessionId(sessionId);
        session.setForgiveness(20);
        session.setStatus("playing");
        session.setCreatedAt(LocalDateTime.now());
        session.setLastActiveAt(LocalDateTime.now());
        session.setHistory(new ArrayList<>());

        // AI 随机生成生气开场白，不使用固定场景池
        String scenario = callAiRaw("请以女友身份说一句生气的话，表达你为什么对男朋友不满。");
        // 清理可能的格式字符
        scenario = scenario
                .replaceAll("得分[：:].*?(\\n|$)", "")
                .replaceAll("原谅值[：:].*?(\\n|$)", "")
                .replaceAll("[(（][^)）]+[)）]", "")
                .trim();
        session.setScenario(scenario);
        session.getHistory().add(Map.of("role", "assistant", "content", scenario));

        sessions.put(sessionId, session);
        log.info("新游戏开始: sessionId={}", sessionId);

        return new GameStartVO(sessionId, scenario, 20);
    }

    @Override
    public GameReplyVO processReply(String sessionId, String content) {
        GameSession session = sessions.get(sessionId);
        if (session == null) {
            throw new RuntimeException("游戏会话不存在或已过期");
        }
        if (!"playing".equals(session.getStatus())) {
            throw new RuntimeException("游戏已结束");
        }

        // 保存用户回复
        session.getHistory().add(Map.of("role", "user", "content", content));
        session.setLastActiveAt(LocalDateTime.now());

        // 调用 AI 获取回复文本
        String aiText = callAi(session, content);

        // 解析 AI 输出并更新状态
        return parseAndUpdate(content, aiText, session);
    }

    /**
     * 核心方法：解析 LLM 输出，更新游戏状态，返回响应。
     * 后端权威计分 —— 不信任 AI 的算术，后端自行计算原谅值。
     */
    private GameReplyVO parseAndUpdate(String userInput, String llmOutput, GameSession session) {
        log.debug("AI 原始输出: {}", llmOutput);

        // 1. 解析得分
        int scoreChange = parseScore(llmOutput);
        scoreChange = Math.max(-10, Math.min(10, scoreChange));

        // 兜底：AI 返回了情绪关键词但忘了写得分
        if (scoreChange == 0) {
            if (llmOutput.contains("非常生气") || llmOutput.contains("愤怒")) {
                scoreChange = -10;
            } else if (llmOutput.contains("开心") || llmOutput.contains("高兴")) {
                scoreChange = 5;
            }
        }

        // 2. 提取对话内容
        
        String dialogue = extractDialogue(llmOutput);

        // 3. 更新原谅值（后端权威计算）
        int newForgiveness = session.getForgiveness() + scoreChange;
        newForgiveness = Math.max(0, Math.min(100, newForgiveness));
        session.setForgiveness(newForgiveness);
        session.setStepCount(session.getStepCount() + 1);

        // 记录对话历史，保留最近 10 轮（20 条消息，含 user + assistant）
        session.getHistory().add(Map.of("role", "assistant", "content", dialogue));
        while (session.getHistory().size() > 20) {
            session.getHistory().remove(0);
        }

        // 4. 判断输赢
        String status = "playing";
        if (newForgiveness >= 100) {
            status = "won";
            session.setStatus("won");
            log.info("游戏胜利: sessionId={}, 步数={}", session.getSessionId(), session.getStepCount());
        } else if (newForgiveness <= 0) {
            status = "lost";
            session.setStatus("lost");
            log.info("游戏失败: sessionId={}, 步数={}", session.getSessionId(), session.getStepCount());
        }

        return new GameReplyVO(dialogue, newForgiveness, scoreChange, status);
    }

    @Override
    public void closeGame(String sessionId) {
        GameSession removed = sessions.remove(sessionId);
        if (removed != null) {
            log.info("游戏会话已关闭: sessionId={}", sessionId);
        }
    }

    /** 从 AI 输出中解析得分 */
    private int parseScore(String output) {
        Matcher matcher = SCORE_PATTERN.matcher(output);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                log.warn("得分解析失败: {}", matcher.group(1));
            }
        }
        return 0;
    }

    /** 从 AI 输出中提取纯对话内容（去掉得分行、原谅值行） */
    private String extractDialogue(String output) {
        String dialogue = output
                .replaceAll("得分[：:].*?(\\n|$)", "")
                .replaceAll("原谅值[：:].*?(\\n|$)", "")
                .replaceAll("[(（][^)）]+[)）]", "")
                .replaceAll("\n{2,}", "\n")
                .trim();

        return dialogue.isEmpty() ? output : dialogue;
    }


    /** 调用 DeepSeek API（游戏对话轮次，带 system prompt + 计分指令） */
    private String callAi(GameSession session, String userMessage) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
                SYSTEM_PROMPT + "\n\n当前场景：" + session.getScenario()
                        + "\n当前原谅值：" + session.getForgiveness() + "/100"));

        // 取最近历史（排除刚添加的最新用户消息，后面手动构建带指令的版本）
        List<Map<String, String>> history = session.getHistory();
        int end = history.size() - 1;
        int start = Math.max(0, end - 7);
        for (int i = start; i < end; i++) {
            messages.add(new HashMap<>(history.get(i)));
        }

        // 用户消息附加显式计分指令（与示例代码一致）
        String prompt = "当前原谅值：" + session.getForgiveness() + "/100\n"
                + "用户对你说：" + userMessage + "\n\n"
                + "请以女友身份回复，并根据规则给出得分。严格按照输出格式。";
        messages.add(Map.of("role", "user", "content", prompt));

        return callDeepSeekApi(messages, 0.8, 512);
    }

    /** 裸调用 AI，用于开场白生成（加一句角色提示） */
    private String callAiRaw(String userMessage) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", "你是一位正在生男朋友气的女友。" + userMessage));
        return callDeepSeekApi(messages, 0.9, 256);
    }

    /** 调用 DeepSeek API 底层 HTTP 请求 */
    private String callDeepSeekApi(List<Map<String, Object>> messages, double temperature, int maxTokens) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelName);
            requestBody.set("messages", objectMapper.valueToTree(messages));
            requestBody.put("temperature", temperature);
            requestBody.put("max_tokens", maxTokens);
            requestBody.put("stream", false);

            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10_000);
            factory.setReadTimeout(30_000);

            RestClient client = restClientBuilder
                    .baseUrl("https://api.deepseek.com/v1")
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .requestFactory(factory)
                    .build();

            String responseBody = client.post()
                    .uri("/chat/completions")
                    .body(requestBody.toString())
                    .retrieve()
                    .body(String.class);

            JsonNode json = objectMapper.readTree(responseBody);
            return json.get("choices").get(0).get("message").get("content").asText();

        } catch (Exception e) {
            throw new RuntimeException("DeepSeek API 调用失败", e);
        }
    }

    @Data
    private static class GameSession {
        private String sessionId;
        private int forgiveness;
        private int stepCount;
        private String scenario;
        private List<Map<String, String>> history;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime lastActiveAt;
    }

    /** 每分钟清理超过 30 分钟不活跃的会话 */
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredSessions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        sessions.values().removeIf(s -> s.getLastActiveAt() == null
                || s.getLastActiveAt().isBefore(cutoff));
        int size = sessions.size();
        if (size > 0) {
            log.debug("当前活跃游戏会话数: {}", size);
        }
    }
}
