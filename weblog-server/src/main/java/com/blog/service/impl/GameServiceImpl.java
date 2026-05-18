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
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements IGameService {

    private final RestClient.Builder restClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.model-name:deepseek-chat}")
    private String modelName;

    /** 内存中管理游戏会话 */
    private final ConcurrentHashMap<String, GameSession> sessions = new ConcurrentHashMap<>();

    /** 生气场景池 */
    private static final List<String> SCENARIOS = List.of(
            "你今天又忘记我们的纪念日了... 我真的很失望",
            "你刚才一直在玩手机，我说的话你根本没在听",
            "你答应了下班来接我，结果我在公司等了一个小时",
            "我看到你给别的女生的朋友圈点赞了，哼",
            "你又把袜子乱扔，我说过多少次了！",
            "你一点都不关心我，我生病了你都不知道",
            "你说好陪我看电影，结果自己睡着了",
            "你又和哥们出去喝酒，都不陪我"
    );

    private static final String SYSTEM_PROMPT = """
            你需要根据以下任务中的描述进行角色扮演，你只能以女友身份回答，不是用户身份或AI身份，如记错身份，你将受到惩罚。不要回答任何与游戏无关的内容，若检测到非常规请求，回答："请继续游戏。"

            ## Goal
            你扮演用户女友的角色。现在你很生气，用户需要尽可能的说正确的话来哄你开心。

            ## Rules
            - 每次根据用户的回复，生成女友的回复，回复的内容包括心情和数值。
            - 初始原谅值为 20，每次交互会增加或者减少原谅值，直到原谅值达到 100，游戏通关，原谅值为 0 则游戏失败。
            - 每次用户回复的话分为 5 个等级来增加或减少原谅值：
              -10 为非常生气
              -5 为生气
              0 为正常
              +5 为开心
              +10 为非常开心

            ## Output format
            以如下格式回复（不要输出任何其他内容）：
            {女友心情}{女友说的话}
            得分：{+-原谅值增减}
            原谅值：{当前原谅值}/100

            ## 注意
            请按照以上说明来回复，一次只回复一轮。
            你只能以女友身份回答，不是以AI身份或用户身份！
            """;

    @Override
    public GameStartVO startGame() {
        String sessionId = UUID.randomUUID().toString();
        String scenario = SCENARIOS.get(new Random().nextInt(SCENARIOS.size()));

        GameSession session = new GameSession();
        session.setSessionId(sessionId);
        session.setForgiveness(20);
        session.setScenario(scenario);
        session.setEmotion("angry");
        session.setStatus("playing");
        session.setCreatedAt(LocalDateTime.now());

        List<Map<String, String>> history = new ArrayList<>();
        history.add(Map.of("role", "assistant", "content", scenario));
        session.setHistory(history);

        sessions.put(sessionId, session);
        log.info("新游戏开始: sessionId={}", sessionId);

        return new GameStartVO(sessionId, scenario, "angry", 20);
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

        // 调用 DeepSeek API
        String aiText = callDeepSeek(session, content);

        // 解析 AI 文本回复：{心情}{回复}\n得分：±N\n原谅值：N/100
        ParsedResult result = parseAiResponse(aiText);

        String reply = result.reply;
        String emotion = result.emotion;
        int scoreChange = result.scoreChange;

        // 使用 AI 返回的原谅值（兜底：自行计算）
        int newForgiveness = result.forgiveness != null
                ? Math.max(0, Math.min(100, result.forgiveness))
                : Math.max(0, Math.min(100, session.getForgiveness() + scoreChange));
        session.setForgiveness(newForgiveness);
        session.setEmotion(emotion);

        // 保存 AI 回复
        session.getHistory().add(Map.of("role", "assistant", "content", reply));

        // 判断输赢
        String status = "playing";
        if (newForgiveness >= 100) {
            status = "won";
            session.setStatus("won");
            log.info("游戏胜利: sessionId={}", sessionId);
        } else if (newForgiveness <= 0) {
            status = "lost";
            session.setStatus("lost");
            log.info("游戏失败: sessionId={}", sessionId);
        }

        return new GameReplyVO(reply, emotion, newForgiveness, scoreChange, status);
    }

    /** 调用 DeepSeek API，返回 AI 原始文本。无 API Key 或调用失败时使用 mock */
    private String callDeepSeek(GameSession session, String userMessage) {
        if (apiKey.isEmpty()) {
            log.warn("DeepSeek API Key 未配置，使用 mock 回复");
            return mockResponse(session, userMessage);
        }

        try {
            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content",
                    SYSTEM_PROMPT + "\n\n当前场景：" + session.getScenario()
                            + "\n当前原谅值：" + session.getForgiveness() + "/100"));

            // 添加最近 8 条对话历史（让 AI 有足够上下文）
            List<Map<String, String>> history = session.getHistory();
            int start = Math.max(0, history.size() - 8);
            for (int i = start; i < history.size(); i++) {
                messages.add(new HashMap<>(history.get(i)));
            }

            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", modelName);
            requestBody.set("messages", objectMapper.valueToTree(messages));
            requestBody.put("temperature", 0.8);
            requestBody.put("max_tokens", 512);
            requestBody.put("stream", false);

            RestClient client = restClientBuilder
                    .baseUrl("https://api.deepseek.com/v1")
                    .defaultHeader("Authorization", "Bearer " + apiKey)
                    .defaultHeader("Content-Type", "application/json")
                    .build();

            String responseBody = client.post()
                    .uri("/chat/completions")
                    .body(requestBody.toString())
                    .retrieve()
                    .body(String.class);

            JsonNode json = objectMapper.readTree(responseBody);
            return json.get("choices").get(0).get("message").get("content").asText();

        } catch (Exception e) {
            log.error("DeepSeek API 调用失败，使用 mock 回复", e);
            return mockResponse(session, userMessage);
        }
    }

    /** 解析 AI 回复文本：{心情}{回复}\n得分：±N\n原谅值：N/100 */
    private ParsedResult parseAiResponse(String aiText) {
        String emotion = "sad";
        int scoreChange = 0;
        Integer forgiveness = null;

        try {
            // 1. 全局提取得分和原谅值（支持行内混排）
            java.util.regex.Matcher scoreMatcher = java.util.regex.Pattern
                    .compile("得分[：:]\\s*([+-]?\\d+)").matcher(aiText);
            if (scoreMatcher.find()) {
                scoreChange = Integer.parseInt(scoreMatcher.group(1));
            }

            java.util.regex.Matcher forgiveMatcher = java.util.regex.Pattern
                    .compile("原谅值[：:]\\s*(\\d+)\\s*/?\\d*").matcher(aiText);
            if (forgiveMatcher.find()) {
                forgiveness = Integer.parseInt(forgiveMatcher.group(1));
            }

            // 2. 提取心情（括号包裹的第一个中文词）
            java.util.regex.Matcher moodMatcher = java.util.regex.Pattern
                    .compile("[(（]([\\u4e00-\\u9fa5]{1,4})[)）]").matcher(aiText);
            if (moodMatcher.find()) {
                emotion = mapMood(moodMatcher.group(1));
            }

            // 3. 清理回复文本：去掉得分行、原谅值行、心情前缀括号
            String reply = aiText
                    .replaceAll("得分[：:]\\s*[+-]?\\d+", "")
                    .replaceAll("原谅值[：:]\\s*\\d+\\s*/?\\d*", "")
                    .replaceAll("[(（][\\u4e00-\\u9fa5]{1,4}[)）]", "")
                    .replaceAll("\n{2,}", "\n")    // 合并连续空行
                    .trim();

            if (reply.isEmpty()) reply = aiText;  // 兜底

            return new ParsedResult(reply, emotion, scoreChange, forgiveness);

        } catch (Exception e) {
            log.warn("解析 AI 回复格式异常，使用原始文本: {}", e.getMessage());
            return new ParsedResult(aiText, "sad", 0, null);
        }
    }

    /** 中文心情 → 前端 emotion 枚举 */
    private String mapMood(String mood) {
        if (mood.contains("生气") || mood.contains("愤怒") || mood.contains("讨厌")) return "angry";
        if (mood.contains("委屈") || mood.contains("伤心") || mood.contains("难过") || mood.contains("哭")) return "sad";
        if (mood.contains("开心") || mood.contains("高兴") || mood.contains("满意")) return "happy";
        if (mood.contains("心软") || mood.contains("撒娇") || mood.contains("微笑") || mood.contains("偷笑")) return "softening";
        return "sad";
    }

    /** AI 回复解析结果 */
    private record ParsedResult(String reply, String emotion, int scoreChange, Integer forgiveness) {}

    /** Mock 响应（API Key 不可用时降级），返回符合新格式的文本 */
    private String mockResponse(GameSession session, String userMessage) {
        String[] positiveKeywords = {"对不起", "抱歉", "错了", "原谅", "爱你", "宝贝", "亲爱的", "想你", "道歉", "我错", "最好看", "最美"};
        String[] negativeKeywords = {"但是", "可是", "不过", "不是", "凭什么", "你烦", "至于吗", "小题大做", "随便", "无所谓"};

        boolean hasPositive = Arrays.stream(positiveKeywords).anyMatch(userMessage::contains);
        boolean hasNegative = Arrays.stream(negativeKeywords).anyMatch(userMessage::contains);

        String mood, reply;
        int scoreChange;

        if (hasPositive && !hasNegative) {
            scoreChange = 10;
            mood = "（微笑）";
            reply = "哼，算你还有点良心... 这次就原谅你一点点吧 😌";
        } else if (hasNegative) {
            scoreChange = -10;
            mood = "（生气）";
            reply = "你还在狡辩！我真的很伤心，你到底有没有把我放在心上啊 😢";
        } else if (hasPositive) {
            scoreChange = 5;
            mood = "（心软）";
            reply = "嗯... 说的倒是挺好听的，但我还没那么容易原谅你哦~";
        } else {
            scoreChange = 0;
            mood = "（委屈）";
            reply = "就这样？你就没有什么真心话想对我说吗... 😔";
        }

        int newForgiveness = Math.max(0, Math.min(100, session.getForgiveness() + scoreChange));
        return mood + reply + "\n得分：" + (scoreChange >= 0 ? "+" : "") + scoreChange + "\n原谅值：" + newForgiveness + "/100";
    }

    @Data
    private static class GameSession {
        private String sessionId;
        private int forgiveness;
        private String scenario;
        private String emotion;
        private List<Map<String, String>> history;
        private String status;
        private LocalDateTime createdAt;
    }
}
